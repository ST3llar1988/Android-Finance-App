package com.example.loaneligibilityindiaandroid.models

data class EligibilityResult(
    val bankName: String,
    val eligibleAmount: Double,
    val requestedAmount: Double,
    val expectedEmi: Double,
    val recommendedRate: Double,
    val confidence: Double,
    val notes: List<String>
) {
    val isEligible: Boolean
        get() = eligibleAmount >= requestedAmount * 0.95
}

