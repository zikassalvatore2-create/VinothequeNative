package com.vinotheque.nativeapp.ui

import android.graphics.BitmapFactory
import android.util.Base64
import android.util.LruCache
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import java.io.File

/**
 * Global LRU bitmap cache — uses 1/4 of available heap for maximum performance.
 * Decoded bitmaps are cached by their string hash to avoid redundant decoding.
 */
object BitmapCache {
    private val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
    private val cacheSize = maxMemory / 4 // Use 25% of available memory

    private val cache = object : LruCache<Int, ImageBitmap>(cacheSize) {
        override fun sizeOf(key: Int, bitmap: ImageBitmap): Int {
            return (bitmap.width * bitmap.height * 4) / 1024 // Approximate KB
        }
    }

    /** Fast synchronous check for already cached images */
    fun getFromMemory(imageData: String?): ImageBitmap? {
        if (imageData == null) return null
        return cache.get(imageData.hashCode())
    }

    /** Decodes image (Base64 or File path) and caches it. Should be called on IO thread. */
    fun get(imageData: String?): ImageBitmap? {
        if (imageData == null) return null
        val key = imageData.hashCode()

        cache.get(key)?.let { return it }

        return try {
            val opts = BitmapFactory.Options().apply {
                inPreferredConfig = android.graphics.Bitmap.Config.ARGB_8888
            }
            val bitmap = if (imageData.startsWith("data:image")) {
                val data = imageData.substringAfter(",")
                val bytes = Base64.decode(data, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opts)
            } else {
                val file = File(imageData)
                if (file.exists()) {
                    BitmapFactory.decodeFile(file.absolutePath, opts)
                } else null
            }
            
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
