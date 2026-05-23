package com.macarambon.pocketledger.screens.login

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.macarambon.pocketledger.R
import com.macarambon.pocketledger.screens.dashboard.DashboardActivity
import com.macarambon.pocketledger.screens.register.RegisterActivity
import com.macarambon.pocketledger.utils.authStore
import com.macarambon.pocketledger.utils.setupPocketLedgerContent
import com.macarambon.pocketledger.utils.startActivity
import com.macarambon.pocketledger.utils.startActivityClearTask
import com.macarambon.pocketledger.utils.toast
import com.macarambon.pocketledger.utils.validateRequired
import com.macarambon.pocketledger.utils.valueText
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout

class LoginActivity : AppCompatActivity(), LoginContract.View {

    private lateinit var presenter: LoginPresenter
    private lateinit var textinputEmailLayout: TextInputLayout
    private lateinit var textinputPasswordLayout: TextInputLayout
    private lateinit var buttonLogin: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupPocketLedgerContent(R.layout.activity_login, R.id.root)

        textinputEmailLayout = findViewById(R.id.textinputEmailLayout)
        textinputPasswordLayout = findViewById(R.id.textinputPasswordLayout)
        buttonLogin = findViewById(R.id.buttonLogin)

        presenter = LoginPresenter(this, LoginModel(authStore()), lifecycleScope, this)

        findViewById<MaterialButton>(R.id.buttonGoRegister).setOnClickListener {
            navigateToRegister()
        }
        buttonLogin.setOnClickListener { submitLogin() }
    }

    private fun submitLogin() {
        val requiredMessage = getString(R.string.error_field_required)
        val okEmail = textinputEmailLayout.validateRequired(requiredMessage)
        val okPassword = textinputPasswordLayout.validateRequired(requiredMessage, trim = false)
        if (!okEmail || !okPassword) return
        presenter.onLoginSubmitted(
            textinputEmailLayout.valueText(),
            textinputPasswordLayout.valueText(trim = false),
        )
    }

    override fun showToast(message: String) = toast(message)

    override fun setLoginInProgress(inProgress: Boolean) {
        buttonLogin.isEnabled = !inProgress
        buttonLogin.text = if (inProgress) {
            getString(R.string.action_logging_in)
        } else {
            getString(R.string.action_login)
        }
    }

    override fun navigateToDashboard() {
        startActivityClearTask(DashboardActivity::class.java)
        finish()
    }

    override fun navigateToRegister() {
        startActivity(RegisterActivity::class.java)
    }
}
