package com.macarambon.pocketledger.screens.dashboard

data class BalanceSummary(
    val balance: Double,
    val startingTotal: Double,
    val netChange: Double,
) {
    val netIsGain: Boolean get() = netChange >= 0
    val netChangeAbs: Double get() = kotlin.math.abs(netChange)
}
