package com.example.loaneligibilityindiaandroid.services

import com.example.loaneligibilityindiaandroid.models.LoanDocType
import com.example.loaneligibilityindiaandroid.models.OcrExtractionResult

object OcrTextParser {
    fun parse(rawText: String, hintType: LoanDocType): OcrExtractionResult {
        val normalized = rawText.replace("\n", " ")

        val monthlyIncome = detectMonthlyIncome(normalized, hintType)
        val annualIncome = detectAnnualIncome(normalized, hintType)
        val existingEmi = detectExistingEmi(normalized)

        val foundCount = listOf(monthlyIncome, annualIncome, existingEmi).count { it != null }
        val confidence = (0.35 + foundCount * 0.2 + (normalized.length / 10000.0).coerceAtMost(0.2)).coerceAtMost(0.96)

        return OcrExtractionResult(
            extractedText = normalized,
            monthlyIncome = monthlyIncome,
            annualItrIncome = annualIncome,
            existingEmi = existingEmi,
            confidence = confidence
        )
    }

    private fun detectMonthlyIncome(text: String, hintType: LoanDocType): Double? {
        val keys = if (hintType == LoanDocType.SALARY_SLIP) {
            listOf("net salary", "in hand", "take home", "monthly salary", "net pay", "gross salary")
        } else {
            listOf("monthly income", "salary")
        }
        return findAmountNear(keys, text)
    }

    private fun detectAnnualIncome(text: String, hintType: LoanDocType): Double? {
        val keys = if (hintType == LoanDocType.ITR) {
            listOf("gross total income", "total income", "income from business", "income from salary")
        } else {
            listOf("annual income", "yearly income")
        }
        return findAmountNear(keys, text)
    }

    private fun detectExistingEmi(text: String): Double? {
        return findAmountNear(listOf("emi", "monthly obligation", "loan installment"), text)
    }

    private fun findAmountNear(keywords: List<String>, text: String): Double? {
        val lower = text.lowercase()
        val regex = Regex("(\\d{1,3}(?:,\\d{2,3})*(?:\\.\\d{1,2})?|\\d+(?:\\.\\d{1,2})?)")
        var best: Double? = null

        keywords.forEach { key ->
            val idx = lower.indexOf(key)
            if (idx < 0) return@forEach

            val start = (idx - 35).coerceAtLeast(0)
            val end = (idx + key.length + 70).coerceAtMost(text.length)
            val window = text.substring(start, end)

            regex.findAll(window).forEach { match ->
                val value = match.value.replace(",", "").toDoubleOrNull()
                if (value != null && value > 1000) {
                    if (best == null || value > best!!) {
                        best = value
                    }
                }
            }
        }

        return best
    }
}

