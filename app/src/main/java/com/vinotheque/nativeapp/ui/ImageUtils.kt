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
fun resizeBitmap(bitmap: Bitmap): String {
    val maxSize = 600
    val ratio = minOf(maxSize.toFloat() / bitmap.width, maxSize.toFloat() / bitmap.height, 1f)
    val w = (bitmap.width * ratio).toInt()
    val h = (bitmap.height * ratio).toInt()
    
    val scaled = Bitmap.createScaledBitmap(bitmap, w, h, true)
    val baos = ByteArrayOutputStream()
    
    // JPEG 75% offers the best balance between quality and file size for mobile screens
    scaled.compress(Bitmap.CompressFormat.JPEG, 75, baos)
    
    val bytes = baos.toByteArray()
    return "data:image/jpeg;base64," + Base64.encodeToString(bytes, Base64.NO_WRAP)
}

/** Opens a web search for the wine label image */
fun searchWineImage(context: Context, wineName: String) {
    if (wineName.isBlank()) return
    val query = Uri.encode("$wineName wine label")
    val url = "https://www.google.com/search?q=$query&tbm=isch"
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    context.startActivity(intent)
}
