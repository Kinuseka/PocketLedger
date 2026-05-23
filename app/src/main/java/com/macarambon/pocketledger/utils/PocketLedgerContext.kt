package com.macarambon.pocketledger.utils

import android.content.Context
import com.macarambon.pocketledger.app.PocketLedgerApplication
import com.macarambon.pocketledger.data.local.PocketLedgerDatabase
import com.macarambon.pocketledger.data.repository.AuthStore

fun Context.requirePocketLedgerApp(): PocketLedgerApplication =
    applicationContext as? PocketLedgerApplication
        ?: error("Application is not PocketLedgerApplication")

fun Context.authStore(): AuthStore = requirePocketLedgerApp().authStore

fun Context.pocketLedgerDb(): PocketLedgerDatabase = requirePocketLedgerApp().database
