package com.qrfromimage

import android.graphics.BitmapFactory
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.module.annotations.ReactModule
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import java.io.File

@ReactModule(name = QrFromImageModule.NAME)
class QrFromImageModule(reactContext: ReactApplicationContext) :
  NativeQrFromImageSpec(reactContext) {

  override fun getName(): String {
    return NAME
  }

  @ReactMethod
  override fun scanFromPath(path: String, promise: Promise) {
    try {
      val rPath = path.replace("file:", "")
      val imgFile = File(rPath)
      if (!imgFile.exists()) {
        promise.reject("", "cannot get image from path: $path")
        return
      }

      val bitmap = BitmapFactory.decodeFile(imgFile.absolutePath)

      // Try ML Kit first
      scanWithMLKit(bitmap, promise) { mlKitFailed ->
        if (mlKitFailed) {
          // Fallback to ZXing
          scanWithZXing(bitmap, promise)
        }
      }

    } catch (e: Exception) {
      promise.reject("ERROR", e.localizedMessage ?: "Unknown error", e)
    }
  }

  private fun scanWithMLKit(bitmap: android.graphics.Bitmap, promise: Promise, onFailure: (Boolean) -> Unit) {
    try {
      val options = BarcodeScannerOptions.Builder()
        .setBarcodeFormats(
          Barcode.FORMAT_QR_CODE,
          Barcode.FORMAT_AZTEC,
          Barcode.FORMAT_DATA_MATRIX,
          Barcode.FORMAT_PDF417,
          Barcode.FORMAT_CODABAR,
          Barcode.FORMAT_CODE_39,
          Barcode.FORMAT_CODE_93,
          Barcode.FORMAT_CODE_128,
          Barcode.FORMAT_EAN_8,
          Barcode.FORMAT_EAN_13,
          Barcode.FORMAT_ITF,
          Barcode.FORMAT_UPC_A,
          Barcode.FORMAT_UPC_E
        )
        .build()

      val image = InputImage.fromBitmap(bitmap, 0)
      val scanner = BarcodeScanning.getClient(options)

      scanner.process(image)
        .addOnSuccessListener { barcodes ->
          if (barcodes.isEmpty()) {
            // No codes found, try ZXing
            onFailure(true)
          } else {
            val codes = barcodes.mapNotNull { it.displayValue }
            val arr = Arguments.fromList(codes)
            promise.resolve(arr)
          }
        }
        .addOnFailureListener {
          // ML Kit failed, trigger fallback
          onFailure(true)
        }
    } catch (e: Exception) {
      onFailure(true)
    }
  }

  private fun scanWithZXing(bitmap: android.graphics.Bitmap, promise: Promise) {
    try {
      val width = bitmap.width
      val height = bitmap.height
      val pixels = IntArray(width * height)
      bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

      val source = RGBLuminanceSource(width, height, pixels)
      val binaryBitmap = BinaryBitmap(HybridBinarizer(source))

      val hints = mapOf(
        DecodeHintType.TRY_HARDER to true,
        DecodeHintType.POSSIBLE_FORMATS to com.google.zxing.BarcodeFormat.values().toList()
      )

      val reader = MultiFormatReader()
      reader.setHints(hints)

      val result = reader.decode(binaryBitmap)
      val codes = listOf(result.text)
      val arr = Arguments.fromList(codes)
      promise.resolve(arr)

    } catch (e: Exception) {
      // Both ML Kit and ZXing failed
      promise.reject("SCAN_FAILED", "Unable to detect any barcodes/QR codes", e)
    }
  }


  companion object {
    const val NAME = "QrFromImage"
  }
}
