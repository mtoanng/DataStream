package com.mtoanng.datastream.util

import android.content.Context
import android.content.res.Resources
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.mtoanng.datastream.DataStreamApp

/** dp -> raw pixels for runtime sizing. */
fun Context.dp(value: Int): Int =
    (value * resources.displayMetrics.density).toInt()

fun Int.dpToPx(): Int =
    (this * Resources.getSystem().displayMetrics.density).toInt()

fun View.show() { visibility = View.VISIBLE }
fun View.hide() { visibility = View.GONE }
fun View.showIf(condition: Boolean) { visibility = if (condition) View.VISIBLE else View.GONE }

fun View.snack(message: String, isError: Boolean = false) {
    Snackbar.make(this, message, if (isError) Snackbar.LENGTH_LONG else Snackbar.LENGTH_SHORT).show()
}

fun Context.toast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

fun Fragment.app(): DataStreamApp =
    requireActivity().application as DataStreamApp
