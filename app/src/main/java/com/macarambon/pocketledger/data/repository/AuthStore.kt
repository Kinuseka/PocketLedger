package com.macarambon.pocketledger.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.macarambon.pocketledger.R
import com.macarambon.pocketledger.app.PocketLedgerApplication
import com.macarambon.pocketledger.data.PocketLedgerResult
import com.macarambon.pocketledger.data.local.PocketLedgerDatabase
import com.macarambon.pocketledger.data.local.entity.UserEntity
import com.macarambon.pocketledger.data.rules.BusinessRules
import com.macarambon.pocketledger.data.security.LocalPasswordHasher

class AuthStore(
    private val sessionPreferences: SessionPreferences? = null,
) {

    private fun session(context: Context): SharedPreferences {
        val app = context.applicationContext
        val holder = sessionPreferences
            ?: (app as? PocketLedgerApplication)?.sessionPreferences
            ?: SessionPreferences(app)
        return holder.prefs
    }

    private fun db(context: Context): PocketLedgerDatabase =
        (context.applicationContext as PocketLedgerApplication).database

    fun isLoggedIn(context: Context): Boolean =
        session(context).getBoolean(KEY_LOGGED_IN, false)

    fun getCurrentUserId(context: Context): Long =
        session(context).getLong(KEY_USER_ID, -1L)

    suspend fun register(
        context: Context,
        firstName: String,
        middleName: String,
        lastName: String,
        email: String,
        password: String,
    ): PocketLedgerResult<Unit> {
        if (!BusinessRules.isValidPersonName(firstName) ||
            !BusinessRules.isValidPersonName(middleName) ||
            !BusinessRules.isValidPersonName(lastName)
        ) {
            return PocketLedgerResult.Err.Validation(context.getString(R.string.error_field_required))
        }
        if (!BusinessRules.isValidEmail(email)) {
            return PocketLedgerResult.Err.Validation(context.getString(R.string.error_invalid_email))
        }
        val trimmedEmail = email.trim()
        val database = db(context)
        val existing = database.userDao().findByEmail(trimmedEmail)
        if (existing != null) {
            return PocketLedgerResult.Err.Validation(context.getString(R.string.error_email_taken))
        }
        val salt = LocalPasswordHasher.generateSalt()
        val hash = LocalPasswordHasher.hash(password, salt)
        database.userDao().insert(
            UserEntity(
                firstName = firstName.trim(),
                middleName = middleName.trim(),
                lastName = lastName.trim(),
                email = trimmedEmail,
                passwordHash = hash,
                passwordSalt = salt,
            ),
        )
        return PocketLedgerResult.Ok(Unit)
    }

    suspend fun signInWithEmail(
        context: Context,
        email: String,
        password: String,
    ): PocketLedgerResult<Unit> {
        val user = db(context).userDao().findByEmail(email.trim())
            ?: return PocketLedgerResult.Err.Validation(context.getString(R.string.error_no_account))
        if (!LocalPasswordHasher.verify(password, user.passwordSalt, user.passwordHash)) {
            return PocketLedgerResult.Err.Validation(context.getString(R.string.error_wrong_credentials))
        }
        session(context).edit()
            .putBoolean(KEY_LOGGED_IN, true)
            .putLong(KEY_USER_ID, user.id)
            .putString(KEY_EMAIL, user.email)
            .apply()
        return PocketLedgerResult.Ok(Unit)
    }

    suspend fun getCurrentUser(context: Context): UserEntity? {
        val userId = getCurrentUserId(context)
        if (userId <= 0L || !isLoggedIn(context)) return null
        return db(context).userDao().findById(userId)
    }

    fun logout(context: Context): PocketLedgerResult<Unit> {
        session(context).edit()
            .putBoolean(KEY_LOGGED_IN, false)
            .remove(KEY_USER_ID)
            .remove(KEY_EMAIL)
            .apply()
        return PocketLedgerResult.Ok(Unit)
    }

    companion object {
        private const val KEY_LOGGED_IN = "logged_in"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_EMAIL = "email"
    }
}
