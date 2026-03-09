package com.example.loaneligibilityindiaandroid.services

import com.example.loaneligibilityindiaandroid.models.ApplicantProfile
import com.example.loaneligibilityindiaandroid.models.EligibilityResult
import com.example.loaneligibilityindiaandroid.models.LoanDocType
import com.example.loaneligibilityindiaandroid.models.LoanDocument

object AiInsightService {
    fun summary(
        profile: ApplicantProfile,
        results: List<EligibilityResult>,
        documents: List<LoanDocument>
    ): String {
        val best = results.firstOrNull() ?: return "Upload documents and fill profile details to generate AI insights."

        val docTypes = documents.map { it.type }.toSet()
        val docsStatus = if (docTypes.contains(LoanDocType.SALARY_SLIP) && docTypes.contains(LoanDocType.ITR)) {
            "Salary slip and ITR uploaded"
        } else {
            "Upload both salary slip and ITR for better confidence"
        }

        val confidence = (best.confidence * 100).toInt()
        val eligibilityPercent = ((best.eligibleAmount / profile.requestedLoanAmount.coerceAtLeast(1.0)) * 100).toInt()

        return "AI Insight: $docsStatus. Best match is ${best.bankName} with ~$confidence% confidence. Approval strength is ~$eligibilityPercent% of requested amount at ~${"%.2f".format(best.recommendedRate)}% interest."
    }
}

