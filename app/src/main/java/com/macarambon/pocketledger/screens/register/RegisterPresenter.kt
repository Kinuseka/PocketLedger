package com.macarambon.pocketledger.screens.register

import android.content.Context
import com.macarambon.pocketledger.R
import com.macarambon.pocketledger.data.notifyErrorIfNotOk
import com.macarambon.pocketledger.utils.ValidationUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegisterPresenter(
    private val view: RegisterContract.View,
    private val model: RegisterModel,
    private val scope: CoroutineScope,
    private val context: Context,
) : RegisterContract.Presenter {

    override fun onRegisterSubmitted(
        firstName: String,
        middleName: String,
        lastName: String,
        email: String,
        password: String,
        confirmPassword: String,
    ) {
        if (password != confirmPassword) {
            view.showToast(context.getString(R.string.error_password_mismatch))
            return
        }
        if (!ValidationUtils.isValidEmail(email)) {
            view.showToast(context.getString(R.string.error_invalid_email))
            return
        }
        view.setRegisterInProgress(true)
        scope.launch {
            val result = withContext(Dispatchers.IO) {
                model.register(context, firstName, middleName, lastName, email, password)
            }
            withContext(Dispatchers.Main) {
                view.setRegisterInProgress(false)
                when (result) {
                    is com.macarambon.pocketledger.data.PocketLedgerResult.Ok -> {
                        view.showToast(context.getString(R.string.success_registered))
                        view.navigateToLogin()
                    }
                    else -> result.notifyErrorIfNotOk { view.showToast(it) }
                }
            }
        }
    }
}
