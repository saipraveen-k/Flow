package com.flowos.app.capture

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Extracts text from TXT (direct), PDF (rendered pages → OCR) and images
 * (direct OCR). Keeps FlowOS's document workflow fully on-device.
 */
class DocumentExtractor(
    private val context: Context,
    private val ocrProcessor: OCRProcessor,
) {

    sealed interface DocumentResult {
        data class Success(val text: String, val persistedPath: String?) : DocumentResult
        data class Failure(val message: String) : DocumentResult
    }

    suspend fun extract(uri: Uri): DocumentResult = withContext(Dispatchers.IO) {
        val name = queryDisplayName(uri) ?: "document"
        val lowerName = name.lowercase()
        when {
            lowerName.endsWith(".txt") -> extractTxt(uri, name)
            lowerName.endsWith(".pdf") -> extractPdf(uri, name)
            lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg") || lowerName.endsWith(".png") ->
                when (val ocr = ocrProcessor.extractText(uri)) {
                    is OCRProcessor.OcrResult.Success -> DocumentResult.Success(ocr.text, null)
                    is OCRProcessor.OcrResult.Failure -> DocumentResult.Failure(ocr.message)
                }

            else -> DocumentResult.Failure("FlowOS reads PDF, TXT, JPG and PNG files.")
        }
    }

    private fun extractTxt(uri: Uri, name: String): DocumentResult =
        try {
            val text = context.contentResolver.openInputStream(uri)?.use { stream ->
                stream.bufferedReader().readText()
            }.orEmpty().trim()
            if (text.isEmpty()) {
                DocumentResult.Failure("That document looks empty.")
            } else {
                DocumentResult.Success(text.take(MAX_TEXT_CHARS), null)
            }
        } catch (_: Exception) {
            DocumentResult.Failure("FlowOS couldn't read $name.")
        }

    private suspend fun extractPdf(uri: Uri, name: String): DocumentResult =
        try {
            val descriptor = context.contentResolver.openFileDescriptor(uri, "r")
                ?: return DocumentResult.Failure("FlowOS couldn't open $name.")
            val text = buildString {
                val renderer = PdfRenderer(descriptor)
                try {
                    val pageCount = minOf(renderer.pageCount, MAX_PDF_PAGES)
                    for (page in 0 until pageCount) {
                        val pdfPage = renderer.openPage(page)
                        val bitmap = renderPage(pdfPage)
                        pdfPage.close()
                        if (bitmap != null) {
                            val pageUri = persistBitmap(bitmap, "pdf_page_$page.png")
                            bitmap.recycle()
                            if (pageUri != null) {
                                when (val ocr = ocrProcessor.extractText(pageUri)) {
                                    is OCRProcessor.OcrResult.Success -> appendLine(ocr.text)
                                    is OCRProcessor.OcrResult.Failure -> Unit
                                }
                            }
                        }
                    }
                } finally {
                    renderer.close()
                    descriptor.close()
                }
            }.trim()

            if (text.isEmpty()) {
                DocumentResult.Failure("No readable text found in $name. Scanned PDFs need clearer pages.")
            } else {
                DocumentResult.Success(text.take(MAX_TEXT_CHARS), null)
            }
        } catch (_: Exception) {
            DocumentResult.Failure("FlowOS couldn't read $name. Try a text-based PDF.")
        }

    private fun renderPage(page: PdfRenderer.Page): Bitmap? =
        try {
            val bitmap = Bitmap.createBitmap(page.width * 2, page.height * 2, Bitmap.Config.ARGB_8888)
            bitmap.eraseColor(android.graphics.Color.WHITE)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            bitmap
        } catch (_: Exception) {
            null
        }

    private fun persistBitmap(bitmap: Bitmap, fileName: String): Uri? =
        try {
            val dir = File(context.cacheDir, "captures").apply { mkdirs() }
            val file = File(dir, fileName)
            file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 90, it) }
            val authority = "${context.packageName}.fileprovider"
            FileProvider.getUriForFile(context, authority, file)
        } catch (_: Exception) {
            null
        }

    private fun queryDisplayName(uri: Uri): String? =
        try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val index = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (index >= 0 && cursor.moveToFirst()) cursor.getString(index) else null
            } ?: uri.lastPathSegment
        } catch (_: Exception) {
            uri.lastPathSegment
        }

    private companion object {
        const val MAX_TEXT_CHARS = 20_000
        const val MAX_PDF_PAGES = 8
    }
}
