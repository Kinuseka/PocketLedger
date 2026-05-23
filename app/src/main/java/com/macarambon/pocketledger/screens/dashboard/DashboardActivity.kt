package com.macarambon.pocketledger.screens.dashboard

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.navigation.NavigationView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.macarambon.pocketledger.R
import com.macarambon.pocketledger.data.local.LedgerEntry
import com.macarambon.pocketledger.data.local.entity.CategoryEntity
import com.macarambon.pocketledger.data.local.entity.WalletEntity
import com.macarambon.pocketledger.screens.login.LoginActivity
import com.macarambon.pocketledger.screens.profile.ProfileActivity
import com.macarambon.pocketledger.screens.transactionhistory.TransactionHistoryActivity
import com.macarambon.pocketledger.screens.wallet.WalletActivity
import com.macarambon.pocketledger.utils.authStore
import com.macarambon.pocketledger.utils.pocketLedgerDb
import com.macarambon.pocketledger.utils.startActivity
import com.macarambon.pocketledger.utils.startActivityClearTask
import com.macarambon.pocketledger.utils.toast
import com.macarambon.pocketledger.utils.validateRequired
import com.macarambon.pocketledger.utils.valueText
import com.macarambon.pocketledger.utils.setCurrentTransactionDateTime
import com.macarambon.pocketledger.utils.showTransactionDateTimePicker
import com.macarambon.pocketledger.utils.currentTransactionDateTimeValue

class DashboardActivity : AppCompatActivity(), DashboardContract.View {

    private lateinit var presenter: DashboardPresenter
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var adapter: LedgerAdapter
    private lateinit var edittextDate: TextInputEditText
    private lateinit var spinnerWalletFilter: Spinner
    private lateinit var spinnerFormWallet: Spinner
    private lateinit var spinnerFormCategory: Spinner
    private lateinit var spinnerTransactionType: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        drawerLayout = findViewById(R.id.drawerLayout)
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        adapter = LedgerAdapter(showWalletName = true) { entry ->
            confirmRemoveTransaction(entry)
        }
        findViewById<RecyclerView>(R.id.recyclerviewLedger).apply {
            layoutManager = LinearLayoutManager(this@DashboardActivity)
            this.adapter = this@DashboardActivity.adapter
        }

        spinnerWalletFilter = findViewById(R.id.spinnerWalletFilter)
        spinnerFormWallet = findViewById(R.id.spinnerFormWallet)
        spinnerFormCategory = findViewById(R.id.spinnerFormCategory)
        spinnerTransactionType = findViewById(R.id.spinnerTransactionType)
        edittextDate = findViewById(R.id.edittextDate)

        presenter = DashboardPresenter(
            this,
            DashboardModel(authStore(), pocketLedgerDb()),
            lifecycleScope,
            this,
        )

        setupTransactionTypeSpinner()
        setupNavigationDrawer()
        setupFormListeners()

        findViewById<MaterialButton>(R.id.buttonAddTransaction).setOnClickListener { submitTransaction() }
        findViewById<MaterialButton>(R.id.buttonAddCategory).setOnClickListener { submitCategory() }
        findViewById<MaterialButton>(R.id.buttonViewAllHistory).setOnClickListener {
            presenter.onViewAllHistoryClicked()
        }
    }

    override fun onResume() {
        super.onResume()
        if (::edittextDate.isInitialized && edittextDate.text.isNullOrBlank()) {
            edittextDate.setCurrentTransactionDateTime()
        }
        presenter.loadDashboard()
    }

    private fun setupTransactionTypeSpinner() {
        spinnerTransactionType.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            listOf(
                getString(R.string.transaction_type_income),
                getString(R.string.transaction_type_expense),
                getString(R.string.transaction_type_interest),
            ),
        )
        spinnerTransactionType.setSelection(1)
    }

    private fun setupFormListeners() {
        spinnerWalletFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                presenter.onWalletFilterSelected(position)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }
        spinnerTransactionType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                presenter.onTransactionTypeSelected(position)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }
        edittextDate.isFocusable = false
        edittextDate.setOnClickListener { showDateTimePicker() }
        edittextDate.setCurrentTransactionDateTime()
    }

    private fun setupNavigationDrawer() {
        findViewById<NavigationView>(R.id.navigationView).setNavigationItemSelectedListener { item ->
            drawerLayout.closeDrawer(GravityCompat.START)
            when (item.itemId) {
                R.id.nav_dashboard -> Unit
                R.id.nav_wallet_management -> navigateToWalletManagement()
                R.id.nav_transaction_history -> navigateToTransactionHistory()
                R.id.nav_profile -> navigateToProfile()
                R.id.nav_logout -> presenter.onLogoutClicked()
            }
            true
        }
    }

    private fun showDateTimePicker() {
        showTransactionDateTimePicker(edittextDate.text?.toString()) { value ->
            edittextDate.setText(value)
        }
    }

    private fun submitTransaction() {
        val amountLayout = findViewById<TextInputLayout>(R.id.textinputAmountLayout)
        val rateLayout = findViewById<TextInputLayout>(R.id.textinputAppliedInterestLayout)
        if (!amountLayout.validateRequired(getString(R.string.error_field_required))) return
        if (edittextDate.text.isNullOrBlank()) {
            toast(getString(R.string.error_field_required))
            return
        }
        presenter.onAddTransactionClicked(
            spinnerFormWallet.selectedItemPosition,
            spinnerFormCategory.selectedItemPosition,
            amountLayout.valueText(),
            edittextDate.currentTransactionDateTimeValue(),
            spinnerTransactionType.selectedItemPosition,
            rateLayout.valueText().ifBlank { "0" },
        )
    }

    private fun submitCategory() {
        val layout = findViewById<TextInputLayout>(R.id.textinputCategoryNameLayout)
        if (!layout.validateRequired(getString(R.string.error_field_required))) return
        presenter.onAddCategoryClicked(layout.valueText())
    }

    override fun showToast(message: String) = toast(message)

    override fun showUserHeader(firstName: String, email: String, initials: String) {
        val header = findViewById<NavigationView>(R.id.navigationView).getHeaderView(0)
        header.findViewById<TextView>(R.id.textviewNavUserName).text = firstName
        header.findViewById<TextView>(R.id.textviewNavEmail).text = email
    }

    override fun showBalanceSummary(summary: BalanceSummary) {
        findViewById<TextView>(R.id.textviewBalance).text =
            getString(R.string.currency_format, String.format("%.2f", summary.balance))
        findViewById<TextView>(R.id.textviewStartedAt).text =
            getString(R.string.label_started_at, getString(R.string.currency_format, String.format("%.2f", summary.startingTotal)))
        val sign = if (summary.netIsGain) "+" else "-"
        val netColor = if (summary.netIsGain) R.color.positive else R.color.negative
        val netLabel = if (summary.netIsGain) getString(R.string.label_gain) else getString(R.string.label_loss)
        findViewById<TextView>(R.id.textviewNetChange).apply {
            text = "$sign${getString(R.string.currency_format, String.format("%.2f", summary.netChangeAbs))} ($netLabel)"
            setTextColor(getColor(netColor))
        }
    }

    override fun showWalletFilterOptions(wallets: List<WalletEntity>, selectedIndex: Int) {
        val names = listOf(getString(R.string.label_all_wallets)) + wallets.map { it.name }
        spinnerWalletFilter.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, names)
        spinnerWalletFilter.setSelection(selectedIndex.coerceAtMost(names.lastIndex))
    }

    override fun showTransactionFormOptions(wallets: List<WalletEntity>, categories: List<CategoryEntity>) {
        spinnerFormWallet.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_dropdown_item, wallets.map { it.name },
        )
        spinnerFormCategory.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_dropdown_item, categories.map { it.name },
        )
    }

    override fun showLedgerPreview(entries: List<LedgerEntry>) {
        adapter.submitList(entries)
    }

    override fun setAppliedInterestVisible(visible: Boolean) {
        findViewById<View>(R.id.layoutAppliedInterestRate).visibility =
            if (visible) View.VISIBLE else View.GONE
    }

    override fun clearTransactionForm() {
        findViewById<TextInputLayout>(R.id.textinputAmountLayout).editText?.text?.clear()
        findViewById<TextInputLayout>(R.id.textinputAppliedInterestLayout).editText?.text?.clear()
        edittextDate.setCurrentTransactionDateTime()
        spinnerTransactionType.setSelection(1)
        setAppliedInterestVisible(false)
    }

    override fun clearCategoryNameField() {
        findViewById<TextInputLayout>(R.id.textinputCategoryNameLayout).editText?.text?.clear()
    }

    override fun navigateToLogin() {
        startActivityClearTask(LoginActivity::class.java)
        finish()
    }

    override fun navigateToWalletManagement() = startActivity(WalletActivity::class.java)
    override fun navigateToTransactionHistory() {
        startActivity(TransactionHistoryActivity::class.java)
    }
    override fun navigateToProfile() = startActivity(ProfileActivity::class.java)

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
