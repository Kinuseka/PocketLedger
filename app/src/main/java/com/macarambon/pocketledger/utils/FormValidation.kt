package com.macarambon.pocketledger.utils

import com.google.android.material.textfield.TextInputLayout

fun TextInputLayout.validateRequired(errorMessage: String, trim: Boolean = true): Boolean {
    val raw = editText?.text?.toString().orEmpty()
    val value = if (trim) raw.trim() else raw
    return if (value.isEmpty()) {
        error = errorMessage
        false
    } else {
        error = null
        true
    }
}

fun TextInputLayout.valueText(trim: Boolean = true): String {
    val raw = editText?.text?.toString().orEmpty()
    return if (trim) raw.trim() else raw
}
