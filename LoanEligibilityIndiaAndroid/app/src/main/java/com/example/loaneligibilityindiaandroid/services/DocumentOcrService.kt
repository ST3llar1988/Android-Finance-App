package com.example.loaneligibilityindiaandroid.services

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Build
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import com.example.loaneligibilityindiaandroid.models.LoanDocType
import com.example.loaneligibilityindiaandroid.models.OcrExtractionResult
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.tasks.await

class DocumentOcrService {
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    suspend fun extractFinancialSignals(context: Context, uri: Uri, hintType: LoanDocType): OcrExtractionResult {
        val image = if (isPdf(context, uri)) extractPdfFirstPageBitmap(context, uri) else extractImageBitmap(context, uri)
            ?: throw IllegalStateException("Unsupported document")

        val input = InputImage.fromBitmap(image, 0)
        val visionText = recognizer.process(input).await().text

        if (visionText.isBlank()) {
            throw IllegalStateException("OCR extraction failed")
        }

        return OcrTextParser.parse(visionText, hintType)
    }

    private fun isPdf(context: Context, uri: Uri): Boolean {
        return context.contentResolver.getType(uri)?.contains("pdf", ignoreCase = true) == true
    }

    private fun extractImageBitmap(context: Context, uri: Uri): Bitmap? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source)
        } else {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        }
    }

    private fun extractPdfFirstPageBitmap(context: Context, uri: Uri): Bitmap? {
        val pfd: ParcelFileDescriptor = context.contentResolver.openFileDescriptor(uri, "r") ?: return null
        pfd.use { descriptor ->
            PdfRenderer(descriptor).use { renderer ->
                if (renderer.pageCount <= 0) return null
                renderer.openPage(0).use { page ->
                    val bitmap = Bitmap.createBitmap(page.width * 2, page.height * 2, Bitmap.Config.ARGB_8888)
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    return bitmap
                }
            }
        }
    }
}

