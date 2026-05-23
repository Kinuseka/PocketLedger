package com.macarambon.pocketledger.app

import android.app.Application
import com.macarambon.pocketledger.data.local.PocketLedgerDatabase
import com.macarambon.pocketledger.data.local.helpers.CategorySeeder
import com.macarambon.pocketledger.data.repository.AuthStore
import com.macarambon.pocketledger.data.repository.SessionPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class PocketLedgerApplication : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val sessionPreferences: SessionPreferences by lazy { SessionPreferences(this) }

    val authStore: AuthStore by lazy { AuthStore(sessionPreferences) }

    val database: PocketLedgerDatabase by lazy { PocketLedgerDatabase.getInstance(this) }

    override fun onCreate() {
        super.onCreate()
        applicationScope.launch {
            CategorySeeder.seedIfEmpty(database)
        }
    }
}
