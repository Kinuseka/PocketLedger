package com.macarambon.pocketledger.utils

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.appcompat.app.AppCompatActivity
import com.macarambon.pocketledger.data.rules.BusinessRules
import java.time.LocalDateTime

fun AppCompatActivity.showTransactionDateTimePicker(
    currentValue: String?,
    onSelected: (String) -> Unit,
) {
    val initial = BusinessRules.parseTransactionDateTime(currentValue.orEmpty()) ?: LocalDateTime.now()
    DatePickerDialog(
        this,
        { _, year, month, day ->
            TimePickerDialog(
                this,
                { _, hour, minute ->
                    val dateTime = LocalDateTime.of(year, month + 1, day, hour, minute)
                    onSelected(BusinessRules.formatTransactionDateTimeInput(dateTime))
                },
                initial.hour,
                initial.minute,
                false,
            ).show()
        },
        initial.year,
        initial.monthValue - 1,
        initial.dayOfMonth,
    ).apply {
        datePicker.maxDate = System.currentTimeMillis()
    }.show()
}
