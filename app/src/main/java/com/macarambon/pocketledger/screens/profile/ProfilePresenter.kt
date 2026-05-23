package com.macarambon.pocketledger.screens.profile

import android.content.Context
import com.macarambon.pocketledger.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfilePresenter(
    private val view: ProfileContract.View,
    private val model: ProfileModel,
    private val scope: CoroutineScope,
    private val context: Context,
) : ProfileContract.Presenter {

    override fun loadProfile() {
        scope.launch {
            val user = withContext(Dispatchers.IO) { model.getCurrentUser(context) }
            if (user == null) {
                view.showToast(context.getString(R.string.error_session_expired))
                view.navigateToLogin()
                return@launch
            }
            val netWorth = withContext(Dispatchers.IO) { model.getNetWorth(user.id) }
            val fullName = listOf(user.firstName, user.middleName, user.lastName)
                .filter { it.isNotBlank() }
                .joinToString(" ")
            withContext(Dispatchers.Main) {
                view.showProfile(
                    fullName,
                    user.email,
                    String.format("%.2f", netWorth),
                )
            }
        }
    }
}
