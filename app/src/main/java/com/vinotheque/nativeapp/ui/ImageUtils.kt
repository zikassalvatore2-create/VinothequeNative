package com.vinotheque.nativeapp.ui

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.util.Base64
import java.io.ByteArrayOutputStream

/**
 * Resizes a bitmap to a maximum dimension and compresses it into a JPEG Base64 string.
 * Using JPEG instead of PNG significantly reduces file size and backup time.
 */
fun resizeBitmap(bitmap: Bitmap, quality: Int = 75): String {
    val maxSize = 800
    val ratio = minOf(maxSize.toFloat() / bitmap.width, maxSize.toFloat() / bitmap.height, 1f)
    val w = (bitmap.width * ratio).toInt()
    val h = (bitmap.height * ratio).toInt()
    
    val scaled = Bitmap.createScaledBitmap(bitmap, w, h, true)
    val baos = ByteArrayOutputStream()
    
    // Use the dynamic quality parameter
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
        scaled.compress(Bitmap.CompressFormat.WEBP_LOSSY, quality.coerceIn(10, 100), baos)
    } else {
        @Suppress("DEPRECATION")
        scaled.compress(Bitmap.CompressFormat.WEBP, quality.coerceIn(10, 100), baos)
    }
    
    val bytes = baos.toByteArray()
    return "data:image/webp;base64," + Base64.encodeToString(bytes, Base64.NO_WRAP)
}

/** 
 * Scans a bitmap and turns pure black pixels (#000000) into transparent pixels.
 * This fixes images that were accidentally blackened during the JPEG phase.
 */
fun fixBlackBackground(bitmap: Bitmap): Bitmap {
    val width = bitmap.width
    val height = bitmap.height
    val pixels = IntArray(width * height)
    bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

    for (i in pixels.indices) {
        val p = pixels[i]
        val r = (p shr 16) and 0xFF
        val g = (p shr 8) and 0xFF
        val b = p and 0xFF
        
        // If it's pure black or extremely close, make it transparent
        if (r < 8 && g < 8 && b < 8) {
            pixels[i] = 0x00000000 // Transparent
        }
    }

    val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    output.setPixels(pixels, 0, width, 0, 0, width, height)
    return output
}

/** Opens a web search for the wine label image */
fun searchWineImage(context: Context, wineName: String) {
    if (wineName.isBlank()) return
    val query = Uri.encode("$wineName wine label")
    val url = "https://www.google.com/search?q=$query&tbm=isch"
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    context.startActivity(intent)
}
