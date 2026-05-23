package com.macarambon.pocketledger.utils

import android.content.Intent
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding

fun AppCompatActivity.toast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

fun AppCompatActivity.startActivity(target: Class<*>) {
    startActivity(Intent(this, target))
}

fun AppCompatActivity.startActivityClearTask(target: Class<*>) {
    startActivity(
        Intent(this, target).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        },
    )
}

fun AppCompatActivity.setupPocketLedgerContent(@LayoutRes layoutRes: Int, @IdRes rootViewId: Int) {
    enableEdgeToEdge()
    setContentView(layoutRes)
    val viewRoot = findViewById<View>(rootViewId)
    ViewCompat.setOnApplyWindowInsetsListener(viewRoot) { view, windowInsets ->
        val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
        view.updatePadding(insets.left, insets.top, insets.right, insets.bottom)
        windowInsets
    }
}
