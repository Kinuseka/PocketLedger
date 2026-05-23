package com.macarambon.pocketledger.screens.transactionhistory

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
import com.google.android.material.navigation.NavigationView
import com.macarambon.pocketledger.R
import com.macarambon.pocketledger.data.local.LedgerEntry
import com.macarambon.pocketledger.screens.dashboard.DashboardActivity
import com.macarambon.pocketledger.screens.dashboard.DashboardModel
import com.macarambon.pocketledger.screens.dashboard.LedgerAdapter
import com.macarambon.pocketledger.screens.login.LoginActivity
import com.macarambon.pocketledger.screens.profile.ProfileActivity
import com.macarambon.pocketledger.screens.wallet.WalletActivity
import com.macarambon.pocketledger.utils.authStore
import com.macarambon.pocketledger.utils.pocketLedgerDb
import com.macarambon.pocketledger.utils.startActivity
import com.macarambon.pocketledger.utils.startActivityClearTask
import com.macarambon.pocketledger.utils.toast

class TransactionHistoryActivity : AppCompatActivity(), TransactionHistoryContract.View {

    private lateinit var presenter: TransactionHistoryPresenter
    private lateinit var adapter: LedgerAdapter
    private var showWalletBadge = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction_history)

        val drawerLayout = findViewById<DrawerLayout>(R.id.drawerLayout)
        findViewById<MaterialToolbar>(R.id.toolbar).apply {
            title = getString(R.string.nav_transaction_history)
            setNavigationOnClickListener { drawerLayout.openDrawer(GravityCompat.START) }
        }

        adapter = LedgerAdapter(showWalletName = true) { entry ->
            confirmRemoveTransaction(entry)
        }
        findViewById<RecyclerView>(R.id.recyclerviewLedger).apply {
            layoutManager = LinearLayoutManager(this@TransactionHistoryActivity)
            this.adapter = this@TransactionHistoryActivity.adapter
        }

        presenter = TransactionHistoryPresenter(
            this,
            TransactionHistoryModel(DashboardModel(authStore(), pocketLedgerDb())),
            lifecycleScope,
            this,
        )

        findViewById<Spinner>(R.id.spinnerWalletFilter).onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    showWalletBadge = position == 0
                    adapter.setShowWalletName(showWalletBadge)
                    presenter.onWalletFilterSelected(position)
                }
                override fun onNothingSelected(parent: AdapterView<*>?) = Unit
            }

        setupDrawer(drawerLayout)
        findViewById<com.google.android.material.button.MaterialButton>(R.id.buttonBackDashboard)
            .setOnClickListener {
                startActivity(DashboardActivity::class.java)
                finish()
            }

        presenter.loadHistory()
    }

    private fun setupDrawer(drawerLayout: DrawerLayout) {
        findViewById<NavigationView>(R.id.navigationView).setNavigationItemSelectedListener { item ->
            drawerLayout.closeDrawer(GravityCompat.START)
            when (item.itemId) {
                R.id.nav_dashboard -> {
                    startActivity(DashboardActivity::class.java)
                    finish()
                }
                R.id.nav_wallet_management -> startActivity(WalletActivity::class.java)
                R.id.nav_transaction_history -> Unit
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

    override fun showToast(message: String) = toast(message)

    override fun showWalletFilterOptions(walletNames: List<String>) {
        findViewById<Spinner>(R.id.spinnerWalletFilter).adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, walletNames)
    }

    override fun showLedger(entries: List<LedgerEntry>) {
        adapter.setShowWalletName(showWalletBadge)
        adapter.submitList(entries)
    }

    override fun navigateToLogin() {
        startActivityClearTask(LoginActivity::class.java)
        finish()
    }

    private fun confirmRemoveTransaction(entry: LedgerEntry) {
        AlertDialog.Builder(this)
            .setTitle(R.string.confirm_delete_transaction_title)
            .setMessage(R.string.confirm_delete_transaction_message)
            .setPositiveButton(R.string.action_remove) { _, _ ->
                presenter.onRemoveTransactionClicked(entry.transactionId)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }
}
