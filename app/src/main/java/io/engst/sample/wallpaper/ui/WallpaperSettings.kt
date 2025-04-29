package io.engst.sample.wallpaper.ui

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Bitmap.Config
import android.graphics.Bitmap.createBitmap
import android.graphics.Canvas
import android.view.WindowManager
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter
import coil3.toBitmap
import io.engst.sample.wallpaper.error
import io.engst.sample.wallpaper.ui.theme.SampleWallpaperTheme
import kotlin.random.Random

fun getRandomImageUri(size: IntSize): String {
  return "https://picsum.photos/seed/${Random.nextInt()}/${size.width}/${size.height}"
}

fun setWallpaper(context: Context, bitmap: Bitmap) {
  try {
    val wallpaperManager = WallpaperManager.getInstance(context)
    wallpaperManager.setBitmap(bitmap)
  } catch (ex: Exception) {
    error(ex) { "Error setting wallpaper" }
  }
}

fun setColorWallpaper(context: Context, color: Int = Random.nextInt()) {
  val bitmap = createBitmap(context.display.width, context.display.height, Config.ARGB_8888)
  Canvas(bitmap).drawColor(color)
  setWallpaper(context, bitmap)
}

fun setImageWallpaper(context: Context, painter: AsyncImagePainter) {
  if (painter.state.value is AsyncImagePainter.State.Success) {
    val bitmap = (painter.state.value as AsyncImagePainter.State.Success).result.image.toBitmap()
    setWallpaper(context, bitmap)
  }
}

fun setLiveWallpaper(context: Context) {
  val intent =
      Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply {
        putExtra(
            WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
            ComponentName(context, SampleWallpaper::class.java))
      }
  context.startActivity(intent)
}

fun getCurrentWallpaper(context: Context): ImageBitmap? {
  val wallpaperManager = WallpaperManager.getInstance(context)
  return try {
    wallpaperManager.drawable?.toBitmap()?.asImageBitmap()
  } catch (ex: Exception) {
    error(ex) { "Error getting current wallpaper" }
    null
  }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WallpaperSettings(modifier: Modifier = Modifier) {
  val context = LocalContext.current
  val wallpaperSize by remember {
    val windowBounds =
        context.getSystemService(WindowManager::class.java).maximumWindowMetrics.bounds
    mutableStateOf(IntSize(windowBounds.width(), windowBounds.height()))
  }

    Surface(
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceAround
        ) {
            Text(
                modifier = Modifier.padding(bottom = 16.dp),
                style = MaterialTheme.typography.titleLarge,
                text = "Wallpaper"
            )

            var imageUri by remember { mutableStateOf(getRandomImageUri(wallpaperSize)) }
            OutlinedCard(modifier = Modifier.padding(8.dp)) {
                val painter = rememberAsyncImagePainter(imageUri)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val state by painter.state.collectAsState()
                    when (state) {
                        is AsyncImagePainter.State.Empty,
                        is AsyncImagePainter.State.Loading -> {
                            CircularProgressIndicator()
                        }

                        is AsyncImagePainter.State.Success -> {
                            Box(modifier = Modifier.fillMaxSize()) {
                                Image(
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop,
                                    painter = painter,
                                    contentDescription = null
                                )
                                IconButton(
                                    modifier = Modifier.align(Alignment.TopEnd),
                                    onClick = { imageUri = getRandomImageUri(wallpaperSize) }) {
                                    Icon(
                                        modifier =
                                            Modifier
                                                .clip(CircleShape)
                                                .background(
                                                    MaterialTheme.colorScheme.surface.copy(alpha = .5f)
                                                ),
                                        imageVector = Icons.Filled.Refresh,
                                        contentDescription = null
                                    )
                                }
                                Button(
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .padding(16.dp),
                                    onClick = { setImageWallpaper(context, painter) }) {
                                    Text(
                                        style = MaterialTheme.typography.bodyLarge,
                                        text = "Set image"
                                    )
                                }
                            }
                        }

                        is AsyncImagePainter.State.Error -> {
                            error((state as AsyncImagePainter.State.Error).result.throwable) { "ooh" }
                            Text(
                                style = MaterialTheme.typography.bodyLarge,
                                text =
                                    "Error: ${(state as AsyncImagePainter.State.Error).result.throwable.message}"
                            )
                        }
                    }
                }
            }

            Button(onClick = { setColorWallpaper(context) }) {
                Text(style = MaterialTheme.typography.bodyLarge, text = "Set random color")
            }
            Button(onClick = { setLiveWallpaper(context) }) {
                Text(style = MaterialTheme.typography.bodyLarge, text = "Set Live wallpaper")
            }
            Button(onClick = { WallpaperManager.getInstance(context).clear() }) {
                Text(style = MaterialTheme.typography.bodyLarge, text = "Clear")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WallpaperSettingsPreview() {
  SampleWallpaperTheme { WallpaperSettings() }
}
