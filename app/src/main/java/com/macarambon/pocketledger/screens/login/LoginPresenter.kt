package com.macarambon.pocketledger.screens.login

import android.content.Context
import com.macarambon.pocketledger.R
import com.macarambon.pocketledger.data.notifyErrorIfNotOk
import com.macarambon.pocketledger.utils.ValidationUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginPresenter(
    private val view: LoginContract.View,
    private val model: LoginModel,
    private val scope: CoroutineScope,
    private val context: Context,
) : LoginContract.Presenter {

    override fun onLoginSubmitted(email: String, password: String) {
        if (!ValidationUtils.isValidEmail(email)) {
            view.showToast(context.getString(R.string.error_invalid_email))
            return
        }
        view.setLoginInProgress(true)
        scope.launch {
            val result = withContext(Dispatchers.IO) {
                model.signIn(context, email, password)
            }
            withContext(Dispatchers.Main) {
                view.setLoginInProgress(false)
                when (result) {
                    is com.macarambon.pocketledger.data.PocketLedgerResult.Ok -> view.navigateToDashboard()
                    else -> result.notifyErrorIfNotOk { view.showToast(it) }
                }
            }
        }
    }
}
