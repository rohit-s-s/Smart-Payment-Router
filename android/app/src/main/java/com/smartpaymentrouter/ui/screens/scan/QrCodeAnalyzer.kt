package com.smartpaymentrouter.ui.screens.scan

import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

class QrCodeAnalyzer(
    private val onQrCodeScanned: (String) -> Unit
) : ImageAnalysis.Analyzer {

    private val scanner = BarcodeScanning.getClient()
    private var isScanning = true

    fun resumeScanning() {
        isScanning = true
    }

    fun pauseScanning() {
        isScanning = false
    }

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        if (!isScanning) {
            imageProxy.close()
            return
        }

        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    val qrCode = barcodes.firstOrNull { 
                        it.format == Barcode.FORMAT_QR_CODE && !it.rawValue.isNullOrBlank() 
                    }
                    if (qrCode?.rawValue != null) {
                        isScanning = false
                        onQrCodeScanned(qrCode.rawValue!!)
                    }
                }
                .addOnFailureListener {
                    // Failures ignored for frame-by-frame analysis
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }
}
