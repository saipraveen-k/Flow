package com.flowos.app.capture

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * On-device OCR via ML Kit (bundled with the app; no network call). Large
 * images are downsampled first so recognition stays fast and memory-safe.
 */
class OCRProcessor(private val context: Context) {

    sealed interface OcrResult {
        data class Success(val text: String, val resized: Boolean) : OcrResult
        data class Failure(val message: String) : OcrResult
    }

    suspend fun extractText(imageUri: Uri): OcrResult = withContext(Dispatchers.IO) {
        try {
            val (bitmap, resized) = loadResizedBitmap(imageUri)
            if (bitmap == null) {
                return@withContext OcrResult.Failure("Couldn't read that image. Try a different file.")
            }
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            val text = try {
                val image = InputImage.fromBitmap(bitmap, 0)
                val result = suspendCancellableCoroutine { continuation ->
                    recognizer.process(image)
                        .addOnSuccessListener { text -> if (continuation.isActive) continuation.resume(text) }
                        .addOnFailureListener { error -> if (continuation.isActive) continuation.resumeWithException(error) }
                }
                result.text.trim()
            } finally {
                recognizer.close()
            }
            if (text.isEmpty()) {
                OcrResult.Failure("FlowOS couldn't understand this capture. Try a clearer image.")
            } else {
                OcrResult.Success(text, resized)
            }
        } catch (_: Exception) {
            OcrResult.Failure("FlowOS couldn't understand this capture. Try a clearer image.")
        }
    }

    private suspend fun loadResizedBitmap(uri: Uri): Pair<Bitmap?, Boolean> =
        withContext(Dispatchers.IO) {
            try {
                val source = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ImageDecoder.createSource(context.contentResolver, uri)
                } else {
                    @Suppress("DEPRECATION")
                    ImageDecoder.createSource(File(uri.path ?: return@withContext null to false))
                }
                var resized = false
                val bitmap = ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                    decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                    val maxDim = maxOf(info.size.width, info.size.height)
                    if (maxDim > MAX_DIMENSION) {
                        val scale = MAX_DIMENSION.toFloat() / maxDim
                        decoder.setTargetSize(
                            (info.size.width * scale).toInt().coerceAtLeast(1),
                            (info.size.height * scale).toInt().coerceAtLeast(1),
                        )
                        resized = true
                    }
                }
                bitmap to resized
            } catch (_: Exception) {
                // Fall back to BitmapFactory for content URIs that ImageDecoder rejects.
                try {
                    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                    context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
                    var sample = 1
                    var maxDim = maxOf(bounds.outWidth, bounds.outHeight)
                    while (maxDim / (sample * 2) >= MAX_DIMENSION / 2) sample *= 2
                    val options = BitmapFactory.Options().apply {
                        inSampleSize = sample
                    }
                    val bitmap = context.contentResolver.openInputStream(uri)?.use {
                        BitmapFactory.decodeStream(it, null, options)
                    }
                    if (bitmap != null) bitmap to (sample > 1) else null to false
                } catch (_: Exception) {
                    null to false
                }
            }
        }

    companion object {
        private const val MAX_DIMENSION = 1600
    }
}
