package com.example.loaneligibilityindiaandroid.services

import com.example.loaneligibilityindiaandroid.models.ApplicantProfile
import com.example.loaneligibilityindiaandroid.models.BankPolicy
import com.example.loaneligibilityindiaandroid.models.EligibilityResult

class EligibilityEngine {
    fun evaluate(profile: ApplicantProfile, banks: List<BankPolicy>): List<EligibilityResult> {
        return banks.map { evaluateBank(profile, it) }.sortedByDescending { it.eligibleAmount }
    }

    private fun evaluateBank(profile: ApplicantProfile, bank: BankPolicy): EligibilityResult {
        val tenure = minOf(profile.preferredTenureYears, bank.maxTenureYears)
        val adjustedIncome = adjustedMonthlyIncome(profile)
        val maxAffordableEmi = maxOf(0.0, adjustedIncome * bank.maxFoir - profile.existingEmi)

        val byEmi = EmiService.maxPrincipalForAffordableEmi(maxAffordableEmi, bank.baseRate, tenure)
        val byLtv = profile.propertyValue * bank.maxLtv

        val notes = mutableListOf<String>()
        if (profile.cibilScore < bank.minCibil) notes += "CIBIL below ${bank.minCibil} threshold"
        if (profile.existingEmi > adjustedIncome * 0.45) notes += "Existing EMI is high relative to income"
        if (profile.preferredTenureYears > bank.maxTenureYears) notes += "Tenure adjusted to ${bank.maxTenureYears} years"

        val cappedByRisk = if (profile.cibilScore >= bank.minCibil) byEmi else byEmi * 0.75
        val eligible = maxOf(0.0, minOf(cappedByRisk, byLtv))
        val projectedEmi = EmiService.calculateEmi(minOf(profile.requestedLoanAmount, eligible), bank.baseRate, tenure)

        return EligibilityResult(
            bankName = bank.name,
            eligibleAmount = eligible,
            requestedAmount = profile.requestedLoanAmount,
            expectedEmi = projectedEmi,
            recommendedRate = bank.baseRate,
            confidence = confidenceScore(profile, bank, notes),
            notes = notes
        )
    }

    private fun adjustedMonthlyIncome(profile: ApplicantProfile): Double {
        val itrMonthly = profile.annualITRIncome / 12.0
        return when (profile.employmentType) {
            com.example.loaneligibilityindiaandroid.models.EmploymentType.SALARIED -> profile.monthlyNetIncome + itrMonthly * 0.25
            com.example.loaneligibilityindiaandroid.models.EmploymentType.SELF_EMPLOYED -> profile.monthlyNetIncome * 0.7 + itrMonthly * 0.65
        }
    }

    private fun confidenceScore(profile: ApplicantProfile, bank: BankPolicy, notes: List<String>): Double {
        var score = 0.7
        if (profile.cibilScore >= bank.minCibil + 40) score += 0.15
        else if (profile.cibilScore < bank.minCibil) score -= 0.2
        if (profile.existingEmi < profile.monthlyNetIncome * 0.2) score += 0.08
        score -= notes.size * 0.05
        return score.coerceIn(0.25, 0.98)
    }
}

