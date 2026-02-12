package com.loanfinancial.lofi.core.common.exception

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Process
import android.util.Log
import com.loanfinancial.lofi.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GlobalExceptionHandler
    @Inject
    constructor(
        private val context: Context,
    ) : Thread.UncaughtExceptionHandler {
        companion object {
            private const val TAG = "GlobalExceptionHandler"
            private const val CRASH_REPORT_DIR = "crash_reports"
            private const val MAX_CRASH_REPORTS = 5
        }

        private val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        private val isDeveloperMode = BuildConfig.DEBUG

        init {
            Thread.setDefaultUncaughtExceptionHandler(this)
        }

        override fun uncaughtException(
            thread: Thread,
            throwable: Throwable,
        ) {
            Log.e(TAG, "Uncaught exception in thread ${thread.name}", throwable)

            // TODO: Temporarily disabled developer error activity for testing
            // Uncomment the following block to re-enable error screenshot functionality

            /*
            if (isDeveloperMode) {
                handleDeveloperModeException(thread, throwable)
            } else {
                handleProductionException(throwable)
            }
             */

            // Always handle as production for now (log only, no UI)
            handleProductionException(throwable)

            defaultHandler?.uncaughtException(thread, throwable)
        }

        private fun handleDeveloperModeException(
            thread: Thread,
            throwable: Throwable,
        ) {
            val crashReport = generateCrashReport(thread, throwable)
            saveCrashReport(crashReport)

            CoroutineScope(Dispatchers.Main).launch {
                try {
                    val intent =
                        Intent(context, DeveloperErrorActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            putExtra("crash_report", crashReport)
                        }
                    context.startActivity(intent)
                    Process.killProcess(Process.myPid())
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to start DeveloperErrorActivity", e)
                }
            }
        }

        private fun handleProductionException(throwable: Throwable) {
            val crashReport = generateCrashReport(Thread.currentThread(), throwable)
            saveCrashReport(crashReport)
            reportToFirebase(throwable, crashReport)
        }

        private fun generateCrashReport(
            thread: Thread,
            throwable: Throwable,
        ): String {
            val stringWriter = StringWriter()
            val printWriter = PrintWriter(stringWriter)

            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val timestamp = dateFormat.format(Date())

            printWriter.println("=== LOFI CRASH REPORT ===")
            printWriter.println("Timestamp: $timestamp")
            printWriter.println("App Version: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
            printWriter.println("Android Version: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
            printWriter.println("Device: ${Build.MANUFACTURER} ${Build.MODEL}")
            printWriter.println("Thread: ${thread.name}")
            printWriter.println()
            printWriter.println("=== STACK TRACE ===")
            throwable.printStackTrace(printWriter)
            printWriter.println()
            printWriter.println("=== END OF REPORT ===")

            return stringWriter.toString()
        }

        private fun saveCrashReport(report: String) {
            try {
                val crashDir = File(context.cacheDir, CRASH_REPORT_DIR)
                if (!crashDir.exists()) {
                    crashDir.mkdirs()
                }

                cleanupOldReports(crashDir)

                val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                val file = File(crashDir, "crash_$timestamp.txt")

                FileWriter(file).use { writer ->
                    writer.write(report)
                }

                Log.d(TAG, "Crash report saved: ${file.absolutePath}")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save crash report", e)
            }
        }

        private fun cleanupOldReports(crashDir: File) {
            try {
                val files =
                    crashDir
                        .listFiles { _, name -> name.startsWith("crash_") && name.endsWith(".txt") }
                        ?.sortedBy { it.lastModified() }
                        ?: return

                if (files.size >= MAX_CRASH_REPORTS) {
                    files.take(files.size - MAX_CRASH_REPORTS + 1).forEach { it.delete() }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to cleanup old crash reports", e)
            }
        }

        private fun reportToFirebase(
            throwable: Throwable,
            crashReport: String,
        ) {
            // TODO: Integrate with Firebase Crashlytics
            // FirebaseCrashlytics.getInstance().recordException(throwable)
            // FirebaseCrashlytics.getInstance().setCustomKey("crash_report", crashReport)
            Log.d(TAG, "Would report to Firebase: $crashReport")
        }

        fun getLastCrashReport(): String? =
            try {
                val crashDir = File(context.cacheDir, CRASH_REPORT_DIR)
                crashDir
                    .listFiles { _, name -> name.startsWith("crash_") && name.endsWith(".txt") }
                    ?.maxByOrNull { it.lastModified() }
                    ?.readText()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to read last crash report", e)
                null
            }

        fun clearCrashReports() {
            try {
                val crashDir = File(context.cacheDir, CRASH_REPORT_DIR)
                crashDir.listFiles()?.forEach { it.delete() }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to clear crash reports", e)
            }
        }
    }
