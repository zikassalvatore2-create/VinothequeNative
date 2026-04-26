package com.vinotheque.nativeapp.ui

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * An asynchronous image loader that decodes images in the background to prevent UI freezing.
 * Works with both legacy Base64 strings and new local file paths.
 */
@Composable
fun AsyncWineImage(
    imageData: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    placeholder: @Composable () -> Unit = {}
) {
    var bitmap by remember(imageData) { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(imageData) {
        if (imageData != null) {
            // Check cache first on main thread
            val cached = BitmapCache.getFromMemory(imageData)
            if (cached != null) {
                bitmap = cached
            } else {
                // Decode in background if not cached
                val decoded = withContext(Dispatchers.IO) {
                    BitmapCache.get(imageData)
                }
                bitmap = decoded
            }
        } else {
            bitmap = null
        }
    }

    if (bitmap != null) {
        Image(
            bitmap = bitmap!!,
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale
        )
    } else {
        placeholder()
    }
}
