package com.example.loaneligibilityindiaandroid.models

enum class LoanDocType(val label: String) {
    SALARY_SLIP("Salary Slip"),
    ITR("ITR")
}

data class LoanDocument(
    val id: String,
    val name: String,
    val type: LoanDocType,
    val uriString: String,
    val uploadedAtMillis: Long,
    val ocrSummary: String? = null,
    val ocrConfidence: Double? = null
)

