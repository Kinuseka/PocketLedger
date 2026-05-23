package com.macarambon.pocketledger.screens.profile

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.macarambon.pocketledger.R
import com.macarambon.pocketledger.screens.login.LoginActivity
import com.macarambon.pocketledger.utils.authStore
import com.macarambon.pocketledger.utils.pocketLedgerDb
import com.macarambon.pocketledger.utils.setupPocketLedgerContent
import com.macarambon.pocketledger.utils.startActivityClearTask
import com.macarambon.pocketledger.utils.toast
import com.google.android.material.button.MaterialButton

class ProfileActivity : AppCompatActivity(), ProfileContract.View {

    private lateinit var presenter: ProfilePresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupPocketLedgerContent(R.layout.activity_profile, R.id.root)

        presenter = ProfilePresenter(
            this,
            ProfileModel(authStore(), pocketLedgerDb()),
            lifecycleScope,
            this,
        )

        findViewById<MaterialButton>(R.id.buttonBack).setOnClickListener { finish() }
        presenter.loadProfile()
    }

    override fun showToast(message: String) = toast(message)

    override fun showProfile(fullName: String, email: String, netWorth: String) {
        findViewById<TextView>(R.id.textviewFullName).text = fullName
        findViewById<TextView>(R.id.textviewEmail).text = email
        findViewById<TextView>(R.id.textviewNetWorth).text =
            getString(R.string.label_net_worth) + ": " + getString(R.string.currency_format, netWorth)
    }

    override fun navigateToLogin() {
        startActivityClearTask(LoginActivity::class.java)
        finish()
    }
}
