package io.engst.sample.wallpaper

import android.util.Log

fun log(message: () -> String) {
    Log.d("bimbam", message())
}

fun error(throwable: Throwable? = null, message: () -> String) {
    Log.e("bimbam", message(), throwable)
}