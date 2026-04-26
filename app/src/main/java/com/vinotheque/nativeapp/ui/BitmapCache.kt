package com.vinotheque.nativeapp.ui

import android.graphics.BitmapFactory
import android.util.Base64
import android.util.LruCache
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

/**
 * Global LRU bitmap cache — uses 1/4 of available heap for maximum performance.
 * Decoded bitmaps are cached by their base64 hash to avoid redundant decoding.
 */
object BitmapCache {
    private val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
    private val cacheSize = maxMemory / 4 // Use 25% of available memory

    private val cache = object : LruCache<Int, ImageBitmap>(cacheSize) {
        override fun sizeOf(key: Int, bitmap: ImageBitmap): Int {
            return (bitmap.width * bitmap.height * 4) / 1024 // Approximate KB
        }
    }

    fun get(base64Image: String?): ImageBitmap? {
        if (base64Image == null) return null
        val key = base64Image.hashCode()

        cache.get(key)?.let { return it }

        return try {
            val data = base64Image.substringAfter(",")
            val bytes = Base64.decode(data, Base64.DEFAULT)
            val opts = BitmapFactory.Options().apply {
                inPreferredConfig = android.graphics.Bitmap.Config.ARGB_8888
            }
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opts)
            bitmap?.asImageBitmap()?.also { cache.put(key, it) }
        } catch (e: Exception) {
            null
        }
    }

    fun clear() {
        cache.evictAll()
    }

    fun size(): Int = cache.size()
    fun maxSize(): Int = cache.maxSize()
}
