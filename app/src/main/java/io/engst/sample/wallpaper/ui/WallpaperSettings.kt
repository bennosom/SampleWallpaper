package io.engst.sample.wallpaper.ui

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap.Config
import android.graphics.Bitmap.createBitmap
import android.graphics.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.engst.sample.wallpaper.error
import io.engst.sample.wallpaper.ui.theme.SampleWallpaperTheme
import kotlin.random.Random

fun setColorWallpaper(context: Context, color: Int = Random.nextInt()) {
    val width = context.display.width
    val height = context.display.height
    val bitmap = createBitmap(width, height, Config.ARGB_8888) // Adjust dimensions as needed
    Canvas(bitmap).drawColor(color)

    try {
        val wallpaperManager = WallpaperManager.getInstance(context)
        wallpaperManager.setBitmap(bitmap)
    } catch (ex: Exception) {
        error(ex) { "Error setting wallpaper" }
    }
}

fun setLiveWallpaper(context: Context) {
    val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply {
        putExtra(
            WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
            ComponentName(context, SampleWallpaper::class.java)
        )
    }
    context.startActivity(intent)
}

@Composable
fun WallpaperSettings(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit = {}
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = MaterialTheme.shapes.medium,
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceAround
            ) {
                val context = LocalContext.current
                Text(
                    modifier = Modifier.padding(bottom = 16.dp),
                    style = MaterialTheme.typography.titleLarge,
                    text = "Set Wallpaper"
                )
                Button(onClick = {
                    setColorWallpaper(context)
                    onDismiss()
                }) {
                    Text(
                        style = MaterialTheme.typography.bodyLarge,
                        text = "Color"
                    )
                }
                Button(onClick = {
                    setLiveWallpaper(context)
                    onDismiss()
                }) {
                    Text(
                        style = MaterialTheme.typography.bodyLarge,
                        text = "Live"
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WallpaperSettingsPreview() {
    SampleWallpaperTheme {
        WallpaperSettings()
    }
}