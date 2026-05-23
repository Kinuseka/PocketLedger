package com.macarambon.pocketledger.screens.register

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.macarambon.pocketledger.R
import com.macarambon.pocketledger.screens.login.LoginActivity
import com.macarambon.pocketledger.utils.authStore
import com.macarambon.pocketledger.utils.setupPocketLedgerContent
import com.macarambon.pocketledger.utils.startActivity
import com.macarambon.pocketledger.utils.toast
import com.macarambon.pocketledger.utils.validateRequired
import com.macarambon.pocketledger.utils.valueText
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout

class RegisterActivity : AppCompatActivity(), RegisterContract.View {

    private lateinit var presenter: RegisterPresenter
    private lateinit var buttonRegister: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupPocketLedgerContent(R.layout.activity_register, R.id.root)

        buttonRegister = findViewById(R.id.buttonRegister)
        presenter = RegisterPresenter(this, RegisterModel(authStore()), lifecycleScope, this)

        findViewById<MaterialButton>(R.id.buttonGoLogin).setOnClickListener {
            finish()
        }
        buttonRegister.setOnClickListener { submitRegister() }
    }

    private fun submitRegister() {
        val requiredMessage = getString(R.string.error_field_required)
        val firstNameLayout = findViewById<TextInputLayout>(R.id.textinputFirstNameLayout)
        val middleNameLayout = findViewById<TextInputLayout>(R.id.textinputMiddleNameLayout)
        val lastNameLayout = findViewById<TextInputLayout>(R.id.textinputLastNameLayout)
        val emailLayout = findViewById<TextInputLayout>(R.id.textinputEmailLayout)
        val passwordLayout = findViewById<TextInputLayout>(R.id.textinputPasswordLayout)
        val confirmLayout = findViewById<TextInputLayout>(R.id.textinputConfirmPasswordLayout)

        val ok = listOf(
            firstNameLayout.validateRequired(requiredMessage),
            middleNameLayout.validateRequired(requiredMessage),
            lastNameLayout.validateRequired(requiredMessage),
            emailLayout.validateRequired(requiredMessage),
            passwordLayout.validateRequired(requiredMessage, trim = false),
            confirmLayout.validateRequired(requiredMessage, trim = false),
        ).all { it }
        if (!ok) return

        presenter.onRegisterSubmitted(
            firstNameLayout.valueText(),
            middleNameLayout.valueText(),
            lastNameLayout.valueText(),
            emailLayout.valueText(),
            passwordLayout.valueText(trim = false),
            confirmLayout.valueText(trim = false),
        )
    }

    override fun showToast(message: String) = toast(message)

    override fun setRegisterInProgress(inProgress: Boolean) {
        buttonRegister.isEnabled = !inProgress
        buttonRegister.text = if (inProgress) {
            getString(R.string.action_registering)
        } else {
            getString(R.string.action_register)
        }
    }

    override fun navigateToLogin() {
        startActivity(LoginActivity::class.java)
        finish()
    }
}
