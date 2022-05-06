package com.example.todo_mvp_kotlin.util

import android.view.View
import com.google.android.material.snackbar.Snackbar

fun View.showSnackBar(message: String, duration: Int) {
    Snackbar.make(this, message, duration).show()
}