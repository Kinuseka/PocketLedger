package com.macarambon.pocketledger.screens.transaction

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.RadioGroup
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.macarambon.pocketledger.R
import com.macarambon.pocketledger.screens.dashboard.DashboardActivity
import com.macarambon.pocketledger.utils.authStore
import com.macarambon.pocketledger.utils.pocketLedgerDb
import com.macarambon.pocketledger.utils.setupPocketLedgerContent
import com.macarambon.pocketledger.utils.startActivity
import com.macarambon.pocketledger.utils.toast
import com.macarambon.pocketledger.utils.validateRequired
import com.macarambon.pocketledger.utils.valueText
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.macarambon.pocketledger.utils.setCurrentTransactionDateTime
import com.macarambon.pocketledger.utils.showTransactionDateTimePicker
import com.macarambon.pocketledger.utils.currentTransactionDateTimeValue

class TransactionActivity : AppCompatActivity(), TransactionContract.View {

    private lateinit var presenter: TransactionPresenter
    private lateinit var layoutAppliedInterest: View
    private lateinit var spinnerWallet: Spinner
    private lateinit var spinnerCategory: Spinner
    private lateinit var radiogroupType: RadioGroup
    private lateinit var edittextDate: TextInputEditText
    private lateinit var buttonSubmit: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupPocketLedgerContent(R.layout.activity_transaction, R.id.root)

        layoutAppliedInterest = findViewById(R.id.layoutAppliedInterestRate)
        spinnerWallet = findViewById(R.id.spinnerWallet)
        spinnerCategory = findViewById(R.id.spinnerCategory)
        radiogroupType = findViewById(R.id.radiogroupTransactionType)
        edittextDate = findViewById(R.id.edittextDate)
        buttonSubmit = findViewById(R.id.buttonSubmitTransaction)

        presenter = TransactionPresenter(
            this,
            TransactionModel(authStore(), pocketLedgerDb()),
            lifecycleScope,
            this,
        )

        edittextDate.isFocusable = false
        edittextDate.setOnClickListener { showDateTimePicker() }
        edittextDate.setCurrentTransactionDateTime()

        radiogroupType.setOnCheckedChangeListener { _, checkedId ->
            val typeIndex = when (checkedId) {
                R.id.radioIncome -> 0
                R.id.radioInterest -> 2
                else -> 1
            }
            presenter.onTransactionTypeSelected(typeIndex)
        }

        buttonSubmit.setOnClickListener { submitTransaction() }
        findViewById<MaterialButton>(R.id.buttonBack).setOnClickListener { finish() }

        presenter.loadFormData()
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
        val typeIndex = when (radiogroupType.checkedRadioButtonId) {
            R.id.radioIncome -> 0
            R.id.radioInterest -> 2
            else -> 1
        }
        presenter.onSubmitClicked(
            spinnerWallet.selectedItemPosition,
            spinnerCategory.selectedItemPosition,
            amountLayout.valueText(),
            edittextDate.currentTransactionDateTimeValue(),
            typeIndex,
            rateLayout.valueText().ifBlank { "0" },
        )
    }

    override fun showToast(message: String) = toast(message)

    override fun setAppliedInterestVisible(visible: Boolean) {
        layoutAppliedInterest.visibility = if (visible) View.VISIBLE else View.GONE
    }

    override fun showWalletOptions(names: List<String>) {
        spinnerWallet.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, names)
    }

    override fun showCategoryOptions(names: List<String>) {
        spinnerCategory.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, names)
    }

    override fun navigateToDashboard() {
        startActivity(DashboardActivity::class.java)
        finish()
    }

    override fun setSubmitInProgress(inProgress: Boolean) {
        buttonSubmit.isEnabled = !inProgress
    }
}
