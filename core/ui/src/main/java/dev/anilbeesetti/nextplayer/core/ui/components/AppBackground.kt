package dev.anilbeesetti.nextplayer.core.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import dev.anilbeesetti.nextplayer.core.model.BackgroundConfig
import dev.anilbeesetti.nextplayer.core.model.BackgroundType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Background of the whole app: the theme's surface colour, or a user-picked picture shown
 * untouched (no blur, no dim) with translucent cards floating above it — the picture is the
 * point, the UI just rides on top.
 */
@Composable
fun AppBackground(
    background: BackgroundConfig,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val surfaceColor = MaterialTheme.colorScheme.surfaceContainer
    when (background.type) {
        BackgroundType.DEFAULT -> Box(
            modifier = modifier.background(color = surfaceColor),
            content = content,
        )

        BackgroundType.IMAGE -> {
            val bitmap = rememberUriBitmap(uriString = background.imageUri)
            Box(modifier = modifier.background(color = surfaceColor)) {
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                }
                content()
            }
        }
    }
}

/**
 * Decodes the picked picture once per URI, downsampled to roughly screen width so a 12 MP photo
 * doesn't turn into a 50 MB bitmap.
 */
@Composable
private fun rememberUriBitmap(uriString: String?): ImageBitmap? {
    val context = LocalContext.current
    val bitmap by produceState<ImageBitmap?>(
        initialValue = null,
        uriString,
    ) {
        value = withContext(context = Dispatchers.IO) {
            if (uriString.isNullOrBlank()) return@withContext null
            runCatching {
                decodeSampledBitmap(
                    context = context,
                    uriString = uriString,
                )
            }
                .getOrNull()
                ?.asImageBitmap()
        }
    }
    return bitmap
}

private fun decodeSampledBitmap(
    context: Context,
    uriString: String,
    requiredWidth: Int = 1440,
): Bitmap? {
    val uri = Uri.parse(uriString)
    val resolver = context.contentResolver
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    resolver.openInputStream(uri)?.use {
        BitmapFactory.decodeStream(
            it,
            null,
            bounds,
        )
    }
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

    var sampleSize = 1
    while (bounds.outWidth / (sampleSize * 2) >= requiredWidth) sampleSize *= 2

    val options = BitmapFactory.Options().apply {
        inSampleSize = sampleSize
        inPreferredConfig = Bitmap.Config.RGB_565
    }
    return resolver.openInputStream(uri)?.use {
        BitmapFactory.decodeStream(
            it,
            null,
            options,
        )
    }
}
