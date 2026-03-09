package com.example.loaneligibilityindiaandroid.models

data class OcrExtractionResult(
    val extractedText: String,
    val monthlyIncome: Double?,
    val annualItrIncome: Double?,
    val existingEmi: Double?,
    val confidence: Double
) {
    val summary: String
        get() {
            val parts = mutableListOf<String>()
            monthlyIncome?.let { parts += "Monthly Income ~ INR ${it.toLong()}" }
            annualItrIncome?.let { parts += "Annual ITR Income ~ INR ${it.toLong()}" }
            existingEmi?.let { parts += "Existing EMI ~ INR ${it.toLong()}" }
            return if (parts.isEmpty()) "No structured values detected" else parts.joinToString(" | ")
        }
}

