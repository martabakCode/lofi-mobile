package com.loanfinancial.lofi.core.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.loanfinancial.lofi.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class LoanSubmissionNotificationManager
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        init {
            createNotificationChannel()
        }

        private fun createNotificationChannel() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel =
                    NotificationChannel(
                        CHANNEL_ID,
                        "Loan Submission",
                        NotificationManager.IMPORTANCE_DEFAULT,
                    ).apply {
                        description = "Notifications for loan submission status"
                    }
                notificationManager.createNotificationChannel(channel)
            }
        }

        fun showSuccessNotification(loanId: String) {
            val notification =
                NotificationCompat
                    .Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_logo)
                    .setContentTitle("Pengajuan Pinjaman Berhasil")
                    .setContentText("Pengajuan pinjaman Anda telah berhasil dikirim ke server.")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true)
                    .build()

            notificationManager.notify(loanId.hashCode(), notification)
        }

        fun showFailureNotification(
            loanId: String,
            reason: String?,
        ) {
            val notification =
                NotificationCompat
                    .Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_logo)
                    .setContentTitle("Pengajuan Pinjaman Gagal")
                    .setContentText("Gagal mengirim pengajuan: ${reason ?: "Tidak ada koneksi internet"}")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true)
                    .build()

            notificationManager.notify(loanId.hashCode(), notification)
        }

        companion object {
            const val CHANNEL_ID = "loan_submission_channel"
        }
    }
