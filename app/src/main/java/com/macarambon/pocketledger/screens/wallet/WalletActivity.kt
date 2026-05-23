package com.macarambon.pocketledger.screens.wallet

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.navigation.NavigationView
import com.google.android.material.textfield.TextInputLayout
import com.macarambon.pocketledger.R
import com.macarambon.pocketledger.data.local.entity.WalletEntity
import com.macarambon.pocketledger.screens.dashboard.DashboardActivity
import com.macarambon.pocketledger.screens.login.LoginActivity
import com.macarambon.pocketledger.screens.profile.ProfileActivity
import com.macarambon.pocketledger.screens.transactionhistory.TransactionHistoryActivity
import com.macarambon.pocketledger.utils.authStore
import com.macarambon.pocketledger.utils.pocketLedgerDb
import com.macarambon.pocketledger.utils.setupPocketLedgerContent
import com.macarambon.pocketledger.utils.startActivity
import com.macarambon.pocketledger.utils.startActivityClearTask
import com.macarambon.pocketledger.utils.toast
import com.macarambon.pocketledger.utils.validateRequired
import com.macarambon.pocketledger.utils.valueText

class WalletActivity : AppCompatActivity(), WalletContract.View {

    private lateinit var presenter: WalletPresenter
    private lateinit var layoutInterestFields: View
    private lateinit var buttonCreateWallet: MaterialButton
    private lateinit var adapter: WalletAdapter
    private lateinit var spinnerWalletType: Spinner
    private lateinit var spinnerInterestFrequency: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupPocketLedgerContent(R.layout.activity_wallet, R.id.root)

        val drawerLayout = findViewById<DrawerLayout>(R.id.drawerLayout)
        findViewById<MaterialToolbar>(R.id.toolbar).setNavigationOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        layoutInterestFields = findViewById(R.id.layoutInterestFields)
        buttonCreateWallet = findViewById(R.id.buttonCreateWallet)
        spinnerWalletType = findViewById(R.id.spinnerWalletType)
        spinnerInterestFrequency = findViewById(R.id.spinnerInterestFrequency)

        adapter = WalletAdapter { wallet ->
            confirmRemoveWallet(wallet)
        }
        findViewById<RecyclerView>(R.id.recyclerviewWallets).apply {
            layoutManager = LinearLayoutManager(this@WalletActivity)
            this.adapter = this@WalletActivity.adapter
        }

        presenter = WalletPresenter(
            this,
            WalletModel(authStore(), pocketLedgerDb()),
            lifecycleScope,
            this,
        )

        setupSpinners()
        setupDrawer(drawerLayout)
        spinnerWalletType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                presenter.onWalletTypeSelected(position)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }

        buttonCreateWallet.setOnClickListener { submitCreateWallet() }
        presenter.loadWallets()
    }

    private fun setupDrawer(drawerLayout: DrawerLayout) {
        findViewById<NavigationView>(R.id.navigationView).setNavigationItemSelectedListener { item ->
            drawerLayout.closeDrawer(GravityCompat.START)
            when (item.itemId) {
                R.id.nav_dashboard -> {
                    startActivity(DashboardActivity::class.java)
                    finish()
                }
                R.id.nav_wallet_management -> Unit
                R.id.nav_transaction_history -> startActivity(TransactionHistoryActivity::class.java)
                R.id.nav_profile -> startActivity(ProfileActivity::class.java)
                R.id.nav_logout -> {
                    authStore().logout(this)
                    startActivityClearTask(LoginActivity::class.java)
                    finish()
                }
            }
            true
        }
    }

    private fun setupSpinners() {
        spinnerWalletType.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            listOf(
                getString(R.string.wallet_type_checking),
                getString(R.string.wallet_type_savings),
                getString(R.string.wallet_type_cash),
            ),
        )
        spinnerInterestFrequency.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            listOf(
                getString(R.string.interest_daily),
                getString(R.string.interest_monthly),
                getString(R.string.interest_yearly),
            ),
        )
    }

    private fun submitCreateWallet() {
        val nameLayout = findViewById<TextInputLayout>(R.id.textinputWalletNameLayout)
        val amountLayout = findViewById<TextInputLayout>(R.id.textinputStartingAmountLayout)
        val rateLayout = findViewById<TextInputLayout>(R.id.textinputInterestRateLayout)
        val required = getString(R.string.error_field_required)
        if (!nameLayout.validateRequired(required)) return
        if (!amountLayout.validateRequired(required)) return

        presenter.onCreateWalletClicked(
            nameLayout.valueText(),
            amountLayout.valueText(),
            spinnerWalletType.selectedItemPosition,
            rateLayout.valueText().ifBlank { "0" },
            spinnerInterestFrequency.selectedItemPosition,
        )
    }

    override fun showToast(message: String) = toast(message)

    override fun showWallets(wallets: List<WalletEntity>) {
        adapter.submitList(wallets)
    }

    override fun setInterestFieldsVisible(visible: Boolean) {
        layoutInterestFields.visibility = if (visible) View.VISIBLE else View.GONE
    }

    override fun clearCreateForm() {
        findViewById<TextInputLayout>(R.id.textinputWalletNameLayout).editText?.text?.clear()
        findViewById<TextInputLayout>(R.id.textinputStartingAmountLayout).editText?.text?.clear()
        findViewById<TextInputLayout>(R.id.textinputInterestRateLayout).editText?.text?.clear()
        spinnerWalletType.setSelection(0)
        setInterestFieldsVisible(false)
    }

    override fun setCreateInProgress(inProgress: Boolean) {
        buttonCreateWallet.isEnabled = !inProgress
    }

    private fun confirmRemoveWallet(wallet: WalletEntity) {
        AlertDialog.Builder(this)
            .setTitle(R.string.confirm_delete_wallet_title)
            .setMessage(getString(R.string.confirm_delete_wallet_message, wallet.name))
            .setPositiveButton(R.string.action_remove) { _, _ ->
                presenter.onRemoveWalletClicked(wallet.id)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }
}
