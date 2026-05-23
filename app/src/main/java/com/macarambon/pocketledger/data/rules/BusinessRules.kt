package com.macarambon.pocketledger.data.rules

import com.macarambon.pocketledger.data.local.entity.InterestFrequency
import com.macarambon.pocketledger.data.local.entity.TransactionType
import com.macarambon.pocketledger.data.local.entity.WalletType
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

object BusinessRules {

    fun isValidPersonName(name: String): Boolean = name.trim().isNotEmpty()

    fun isValidEmail(email: String): Boolean =
        Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$").matches(email.trim())

    fun normalizeCategoryName(name: String): String = name.trim()

    fun isValidWalletName(name: String): Boolean = name.trim().isNotEmpty()

    /** Transaction amounts must be strictly greater than zero. */
    fun isValidTransactionAmount(amount: Double): Boolean = amount > 0.0

    /** Wallet starting balance may be zero or positive. */
    fun isValidStartingBalance(amount: Double): Boolean = amount >= 0.0

    fun isValidInterestRate(rate: Double): Boolean = rate in 0.0..100.0

    fun parseTransactionDateTime(value: String): LocalDateTime? {
        if (value.isBlank()) return null
        return try {
            LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        } catch (_: DateTimeParseException) {
            try {
                LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE).atStartOfDay()
            } catch (_: DateTimeParseException) {
                try {
                    LocalDateTime.parse(value, DISPLAY_FORMATTER)
                } catch (_: DateTimeParseException) {
                    null
                }
            }
        }
    }

    fun defaultTransactionDateTime(): LocalDateTime = LocalDateTime.now()

    fun defaultTransactionDateTimeDisplay(): String =
        defaultTransactionDateTime().format(DISPLAY_FORMATTER)

    fun toStoredTransactionDateTime(value: String): String =
        parseTransactionDateTime(value)?.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            ?: defaultTransactionDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

    fun formatTransactionDateTimeInput(dateTime: LocalDateTime): String =
        dateTime.format(DISPLAY_FORMATTER)

    fun isFutureTransactionDateTime(value: String): Boolean {
        val parsed = parseTransactionDateTime(value) ?: return true
        return parsed.isAfter(LocalDateTime.now())
    }

    fun formatTransactionDateTime(value: String): String {
        return parseTransactionDateTime(value)?.format(DISPLAY_FORMATTER) ?: value
    }

    fun requiresWalletInterest(type: WalletType): Boolean = type == WalletType.SAVINGS

    fun requiresAppliedInterestRate(type: TransactionType): Boolean = type == TransactionType.INTEREST

    fun walletInterestFrequencyOrNull(
        type: WalletType,
        frequency: InterestFrequency?,
    ): InterestFrequency? = if (type == WalletType.SAVINGS) frequency else null

    fun walletInterestRateOrNull(type: WalletType, rate: Double?): Double? =
        if (type == WalletType.SAVINGS) rate else null

    fun appliedInterestRateOrNull(type: TransactionType, rate: Double?): Double? =
        if (type == TransactionType.INTEREST) rate else null

    private val DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a")
}
