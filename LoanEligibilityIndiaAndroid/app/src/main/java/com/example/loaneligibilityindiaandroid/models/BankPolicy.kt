package com.example.loaneligibilityindiaandroid.models

data class BankPolicy(
    val name: String,
    val baseRate: Double,
    val maxTenureYears: Int,
    val maxLtv: Double,
    val maxFoir: Double,
    val minCibil: Int
) {
    companion object {
        val indianHomeLoanBanks = listOf(
            BankPolicy("State Bank of India", 8.55, 30, 0.90, 0.55, 700),
            BankPolicy("HDFC Bank", 8.75, 30, 0.90, 0.60, 700),
            BankPolicy("ICICI Bank", 8.80, 30, 0.85, 0.58, 705),
            BankPolicy("Axis Bank", 8.90, 30, 0.85, 0.58, 700),
            BankPolicy("Punjab National Bank", 8.65, 30, 0.85, 0.55, 680)
        )
    }
}

