package com.macarambon.pocketledger.data.repository

import android.content.Context
import android.content.SharedPreferences

class SessionPreferences(context: Context) {
    private val appContext = context.applicationContext

    val prefs: SharedPreferences =
        appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        const val PREFS_NAME = "pocketledger_session"
    }
}
