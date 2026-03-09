package com.example.loaneligibilityindiaandroid.models

enum class EmploymentType(val label: String) {
    SALARIED("Salaried"),
    SELF_EMPLOYED("Self Employed")
}

data class ApplicantProfile(
    val fullName: String = "",
    val employmentType: EmploymentType = EmploymentType.SALARIED,
    val monthlyNetIncome: Double = 120000.0,
    val annualITRIncome: Double = 1800000.0,
    val existingEmi: Double = 15000.0,
    val cibilScore: Int = 760,
    val propertyValue: Double = 7500000.0,
    val requestedLoanAmount: Double = 5000000.0,
    val preferredTenureYears: Int = 20
)

