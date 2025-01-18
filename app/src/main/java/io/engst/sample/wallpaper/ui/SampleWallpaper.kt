package io.engst.sample.wallpaper.ui

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import android.os.Handler
import android.service.wallpaper.WallpaperService
import android.view.MotionEvent
import android.view.SurfaceHolder
import io.engst.sample.wallpaper.log


class SampleWallpaper : WallpaperService() {

    override fun onCreateEngine(): Engine {
        return WallpaperEngine()
    }

    inner class WallpaperEngine : Engine() {
        private lateinit var holder: SurfaceHolder
        private lateinit var paint: Paint
        private lateinit var handler: Handler
        private var isVisible = false

        private var x: Float = 0f
        private var y: Float = 0f
        private var radius: Float = 1f
        private var radiusIncrease = true

        private val drawRunner = object : Runnable {
            override fun run() {
                if (isVisible) {
                    holder.lockCanvas()?.let {
                        try {
                            draw(it)
                        } finally {
                            holder.unlockCanvasAndPost(it)
                        }
                    }

                    if (radius > 200f) {
                        radiusIncrease = false
                    }
                    if (radius <= 50f) {
                        radiusIncrease = true
                    }
                    radius = (radius + if (radiusIncrease) 2f else -2f).coerceAtLeast(1f)

                    handler.postDelayed(this, 16)
                }
            }
        }

        override fun onCreate(surfaceHolder: SurfaceHolder) {
            log { "onCreate: ${surfaceHolder.surface}" }
            super.onCreate(surfaceHolder)
            holder = surfaceHolder

            paint = Paint().apply {
                style = Paint.Style.FILL
                isAntiAlias = true
            }

            handler = Handler(mainLooper)
            handler.post(drawRunner)
        }

        override fun onSurfaceCreated(holder: SurfaceHolder?) {
            log { "onSurfaceCreated: ${holder?.surface} is using ${displayContext?.display}" }
            super.onSurfaceCreated(holder)

            surfaceHolder.lockCanvas()?.let {
                x = it.width / 2f
                y = it.height / 2f
                surfaceHolder.unlockCanvasAndPost(it)
            }
        }

        override fun onSurfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            log { "onSurfaceChanged: ${holder.surface}" }
            super.onSurfaceChanged(holder, format, width, height)
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder?) {
            log { "onSurfaceDestroyed: ${holder?.surface}" }
            super.onSurfaceDestroyed(holder)
            isVisible = false
            handler.removeCallbacks(drawRunner)
        }

        override fun onDestroy() {
            log { "onDestroy" }
            super.onDestroy()
            isVisible = false
            handler.removeCallbacks(drawRunner)
        }

        override fun onVisibilityChanged(visible: Boolean) {
            log { "onVisibilityChanged: $visible" }
            super.onVisibilityChanged(visible)
            isVisible = visible
            if (visible) {
                handler.post(drawRunner)
            } else {
                handler.removeCallbacks(drawRunner)
            }
        }

        override fun onTouchEvent(event: MotionEvent?) {
            super.onTouchEvent(event)
            x = event?.x ?: 0f
            y = event?.y ?: 0f
        }

        private fun createGradient(radius: Float): RadialGradient {
            val colors = intArrayOf(Color.WHITE, Color.YELLOW, Color.TRANSPARENT)
            val stops = floatArrayOf(0f, 0.7f, 1f)
            return RadialGradient(x, y, radius, colors, stops, Shader.TileMode.CLAMP)
        }

        private fun draw(canvas: Canvas) {
            paint.shader = createGradient(radius)

            canvas.drawColor(Color.BLACK)
            canvas.drawCircle(x, y, radius, paint)
        }
    }
}