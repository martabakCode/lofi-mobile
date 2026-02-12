package com.loanfinancial.lofi.core.media

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.FragmentActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

sealed class CameraResult {
    data class Success(
        val filePath: String,
        val uri: Uri,
    ) : CameraResult()

    data class Error(
        val message: String,
    ) : CameraResult()

    data object Cancelled : CameraResult()
}

sealed class GalleryResult {
    data class Success(
        val filePath: String,
        val uri: Uri,
    ) : GalleryResult()

    data class Error(
        val message: String,
    ) : GalleryResult()

    data object Cancelled : GalleryResult()
}

enum class DocumentType {
    KK,
    KTP,
    PAYSLIP,
    PROOFOFRESIDENCE,
    BANK_STATEMENT,
    OTHER,
    PROFILE_PICTURE,
    NPWP,
    ;

    val backendName: String
        get() =
            when (this) {
                KTP -> "KTP"
                KK -> "KK"
                NPWP -> "NPWP"
                PAYSLIP -> "PAYSLIP"
                PROOFOFRESIDENCE -> "PROOFOFRESIDENCE"
                BANK_STATEMENT -> "BANK_STATEMENT"
                PROFILE_PICTURE -> "PROFILE_PICTURE"
                OTHER -> "OTHER"
            }
}

interface CameraManager {
    suspend fun captureImage(documentType: DocumentType): Flow<CameraResult>

    suspend fun selectFromGallery(documentType: DocumentType): Flow<GalleryResult>

    suspend fun compressImage(
        imagePath: String,
        maxSizeKB: Int,
    ): String

    fun createTempImageFile(documentType: DocumentType): File

    fun getUriForFile(file: File): Uri
}

@Singleton
class CameraManagerImpl
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) : CameraManager {
        private val activity: FragmentActivity?
            get() = context as? FragmentActivity

        override fun createTempImageFile(documentType: DocumentType): File {
            val timeStamp = System.currentTimeMillis()
            val fileName = "${documentType.name}_$timeStamp.jpg"
            val storageDir =
                File(context.cacheDir, "camera_images").apply {
                    if (!exists()) mkdirs()
                }
            return File(storageDir, fileName)
        }

        override fun getUriForFile(file: File): Uri =
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file,
            )

        override suspend fun captureImage(documentType: DocumentType): Flow<CameraResult> =
            flow {
                val activity =
                    this@CameraManagerImpl.activity
                        ?: run {
                            emit(CameraResult.Error("Activity not available"))
                            return@flow
                        }

                val tempFile = createTempImageFile(documentType)
                val photoUri = getUriForFile(tempFile)

                val result =
                    kotlinx.coroutines.suspendCancellableCoroutine<CameraResult> { continuation ->
                        val launcher =
                            activity.activityResultRegistry.register(
                                "camera_${System.currentTimeMillis()}",
                                ActivityResultContracts.TakePicture(),
                            ) { success ->
                                if (success) {
                                    continuation.resume(
                                        CameraResult.Success(tempFile.absolutePath, photoUri),
                                    )
                                } else {
                                    tempFile.delete()
                                    continuation.resume(CameraResult.Cancelled)
                                }
                            }

                        launcher.launch(photoUri)

                        continuation.invokeOnCancellation {
                            launcher.unregister()
                            tempFile.delete()
                        }
                    }

                emit(result)
            }

        override suspend fun selectFromGallery(documentType: DocumentType): Flow<GalleryResult> =
            flow {
                val activity =
                    this@CameraManagerImpl.activity
                        ?: run {
                            emit(GalleryResult.Error("Activity not available"))
                            return@flow
                        }

                val result =
                    kotlinx.coroutines.suspendCancellableCoroutine<GalleryResult> { continuation ->
                        val launcher =
                            activity.activityResultRegistry.register(
                                "gallery_${System.currentTimeMillis()}",
                                ActivityResultContracts.GetContent(),
                            ) { uri ->
                                if (uri != null) {
                                    val filePath = copyUriToFile(uri, documentType)
                                    if (filePath != null) {
                                        continuation.resume(
                                            GalleryResult.Success(filePath, uri),
                                        )
                                    } else {
                                        continuation.resume(
                                            GalleryResult.Error("Failed to copy file"),
                                        )
                                    }
                                } else {
                                    continuation.resume(GalleryResult.Cancelled)
                                }
                            }

                        launcher.launch("image/*")

                        continuation.invokeOnCancellation {
                            launcher.unregister()
                        }
                    }

                emit(result)
            }

        private fun copyUriToFile(
            uri: Uri,
            documentType: DocumentType,
        ): String? =
            try {
                val tempFile = createTempImageFile(documentType)
                context.contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(tempFile).use { output ->
                        input.copyTo(output)
                    }
                }
                tempFile.absolutePath
            } catch (e: Exception) {
                null
            }

        override suspend fun compressImage(
            imagePath: String,
            maxSizeKB: Int,
        ): String {
            return withContext(Dispatchers.IO) {
                val file = File(imagePath)
                if (!file.exists()) {
                    throw IllegalArgumentException("File does not exist: $imagePath")
                }

                val maxSizeBytes = maxSizeKB * 1024

                if (file.length() <= maxSizeBytes) {
                    return@withContext imagePath
                }

                val bitmap =
                    BitmapFactory.decodeFile(imagePath)
                        ?: throw IllegalArgumentException("Failed to decode image")

                var quality = 100
                var compressedBytes: ByteArray

                do {
                    val outputStream = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
                    compressedBytes = outputStream.toByteArray()
                    quality -= 5
                } while (compressedBytes.size > maxSizeBytes && quality > 5)

                val compressedFile =
                    File(
                        file.parent,
                        "compressed_${file.name}",
                    )

                FileOutputStream(compressedFile).use { output ->
                    output.write(compressedBytes)
                }

                bitmap.recycle()

                compressedFile.absolutePath
            }
        }
    }
