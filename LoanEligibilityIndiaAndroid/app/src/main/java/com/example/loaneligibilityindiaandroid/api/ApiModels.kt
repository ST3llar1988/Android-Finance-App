package com.example.loaneligibilityindiaandroid.api

import com.example.loaneligibilityindiaandroid.models.ApplicantProfile
import com.example.loaneligibilityindiaandroid.models.BankPolicy
import com.example.loaneligibilityindiaandroid.models.EligibilityResult

data class ApiConfig(
    val baseUrl: String = "https://api.loaneligibilityindia.com",
    val apiKey: String? = null
)

fun ApplicantProfile.toRequestMap(): Map<String, Any> = mapOf(
    "fullName" to fullName,
    "employmentType" to employmentType.label,
    "monthlyNetIncome" to monthlyNetIncome,
    "annualITRIncome" to annualITRIncome,
    "existingEMI" to existingEmi,
    "cibilScore" to cibilScore,
    "propertyValue" to propertyValue,
    "requestedLoanAmount" to requestedLoanAmount,
    "preferredTenureYears" to preferredTenureYears
)

fun bankPolicyFromJson(
    name: String,
    baseRate: Double,
    maxTenureYears: Int,
    maxLtv: Double,
    maxFoir: Double,
    minCibil: Int
): BankPolicy = BankPolicy(name, baseRate, maxTenureYears, maxLtv, maxFoir, minCibil)

fun eligibilityResultFromJson(
    bankName: String,
    eligibleAmount: Double,
    requestedAmount: Double,
    expectedEmi: Double,
    recommendedRate: Double,
    confidence: Double,
    notes: List<String>
): EligibilityResult = EligibilityResult(
    bankName = bankName,
    eligibleAmount = eligibleAmount,
    requestedAmount = requestedAmount,
    expectedEmi = expectedEmi,
    recommendedRate = recommendedRate,
    confidence = confidence,
    notes = notes
)

