package com.macarambon.pocketledger.utils

import com.google.android.material.textfield.TextInputEditText
import com.macarambon.pocketledger.data.rules.BusinessRules

fun TextInputEditText.setCurrentTransactionDateTime() {
    setText(BusinessRules.defaultTransactionDateTimeDisplay())
}

fun TextInputEditText.currentTransactionDateTimeValue(): String =
    BusinessRules.toStoredTransactionDateTime(text?.toString().orEmpty())
