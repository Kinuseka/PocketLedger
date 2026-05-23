package com.macarambon.pocketledger.utils

import com.macarambon.pocketledger.data.rules.BusinessRules

object ValidationUtils {
    fun isValidEmail(email: String): Boolean = BusinessRules.isValidEmail(email)
}
