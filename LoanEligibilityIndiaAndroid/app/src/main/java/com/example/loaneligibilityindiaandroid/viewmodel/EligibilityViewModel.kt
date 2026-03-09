package com.example.loaneligibilityindiaandroid.viewmodel

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.loaneligibilityindiaandroid.api.EligibilityApiClient
import com.example.loaneligibilityindiaandroid.models.ApplicantProfile
import com.example.loaneligibilityindiaandroid.models.BankPolicy
import com.example.loaneligibilityindiaandroid.models.EligibilityResult
import com.example.loaneligibilityindiaandroid.models.EmploymentType
import com.example.loaneligibilityindiaandroid.models.LoanDocType
import com.example.loaneligibilityindiaandroid.models.LoanDocument
import com.example.loaneligibilityindiaandroid.models.OcrExtractionResult
import com.example.loaneligibilityindiaandroid.services.AiInsightService
import com.example.loaneligibilityindiaandroid.services.DocumentOcrService
import com.example.loaneligibilityindiaandroid.services.EmiService
import com.example.loaneligibilityindiaandroid.services.EligibilityEngine
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EligibilityViewModel : ViewModel() {
    var profile by mutableStateOf(ApplicantProfile())
        private set

    var documents by mutableStateOf(listOf<LoanDocument>())
        private set

    var results by mutableStateOf(listOf<EligibilityResult>())
        private set

    var aiSummary by mutableStateOf("")
        private set

    var statusMessage by mutableStateOf("")
        private set

    var selectedDocType by mutableStateOf(LoanDocType.SALARY_SLIP)
    var isProcessingDocument by mutableStateOf(false)
        private set
    var isCalculating by mutableStateOf(false)
        private set

    var useLiveBankPolicies by mutableStateOf(false)
    var useLiveEligibilityApi by mutableStateOf(false)

    var simulationPrincipal by mutableStateOf(5000000.0)
    var simulationRate by mutableStateOf(8.75)
    var simulationTenureYears by mutableStateOf(20)

    val simulatedEmi: Double
        get() = EmiService.calculateEmi(simulationPrincipal, simulationRate, simulationTenureYears)

    private val eligibilityEngine = EligibilityEngine()
    private val ocrService = DocumentOcrService()
    private val apiClient = EligibilityApiClient()

    fun setFullName(value: String) { profile = profile.copy(fullName = value) }
    fun setEmploymentType(value: EmploymentType) { profile = profile.copy(employmentType = value) }
    fun setMonthlyIncome(value: String) { parseDouble(value)?.let { profile = profile.copy(monthlyNetIncome = it) } }
    fun setAnnualItrIncome(value: String) { parseDouble(value)?.let { profile = profile.copy(annualITRIncome = it) } }
    fun setExistingEmi(value: String) { parseDouble(value)?.let { profile = profile.copy(existingEmi = it) } }
    fun setPropertyValue(value: String) { parseDouble(value)?.let { profile = profile.copy(propertyValue = it) } }
    fun setRequestedAmount(value: String) { parseDouble(value)?.let { profile = profile.copy(requestedLoanAmount = it) } }
    fun setCibil(value: Int) { profile = profile.copy(cibilScore = value) }
    fun setTenure(value: Int) { profile = profile.copy(preferredTenureYears = value) }

    fun onDocumentPicked(context: Context, uri: Uri) {
        viewModelScope.launch {
            isProcessingDocument = true
            try {
                val extraction = withContext(Dispatchers.IO) {
                    ocrService.extractFinancialSignals(context, uri, selectedDocType)
                }

                applyExtraction(extraction)

                val document = LoanDocument(
                    id = UUID.randomUUID().toString(),
                    name = uri.lastPathSegment ?: "document",
                    type = selectedDocType,
                    uriString = uri.toString(),
                    uploadedAtMillis = System.currentTimeMillis(),
                    ocrSummary = extraction.summary,
                    ocrConfidence = extraction.confidence
                )
                documents = documents + document
                statusMessage = "OCR extraction complete for ${selectedDocType.label}"
            } catch (_: Exception) {
                val fallback = LoanDocument(
                    id = UUID.randomUUID().toString(),
                    name = uri.lastPathSegment ?: "document",
                    type = selectedDocType,
                    uriString = uri.toString(),
                    uploadedAtMillis = System.currentTimeMillis()
                )
                documents = documents + fallback
                statusMessage = "Uploaded file, but OCR could not extract structured values"
            } finally {
                isProcessingDocument = false
            }
        }
    }

    fun removeDocument(id: String) {
        documents = documents.filterNot { it.id == id }
    }

    fun calculateEligibility() {
        viewModelScope.launch {
            isCalculating = true
            try {
                if (useLiveEligibilityApi) {
                    val remoteResults = withContext(Dispatchers.IO) { apiClient.calculateEligibility(profile) }
                    results = remoteResults
                    aiSummary = AiInsightService.summary(profile, results, documents)
                    statusMessage = "Calculated eligibility via live API"
                    return@launch
                }

                val activePolicies = if (useLiveBankPolicies) {
                    try {
                        withContext(Dispatchers.IO) { apiClient.fetchBankPolicies() }.ifEmpty { BankPolicy.indianHomeLoanBanks }
                    } catch (_: Exception) {
                        statusMessage = "Live policy API failed, using local defaults"
                        BankPolicy.indianHomeLoanBanks
                    }
                } else {
                    BankPolicy.indianHomeLoanBanks
                }

                results = eligibilityEngine.evaluate(profile, activePolicies)
                aiSummary = AiInsightService.summary(profile, results, documents)
                statusMessage = "Calculated eligibility for ${results.size} banks"
            } catch (_: Exception) {
                statusMessage = "Eligibility calculation failed"
            } finally {
                isCalculating = false
            }
        }
    }

    private fun parseDouble(value: String): Double? {
        return value.replace(",", "").trim().toDoubleOrNull()
    }

    private fun applyExtraction(extraction: OcrExtractionResult) {
        profile = profile.copy(
            monthlyNetIncome = extraction.monthlyIncome ?: profile.monthlyNetIncome,
            annualITRIncome = extraction.annualItrIncome ?: profile.annualITRIncome,
            existingEmi = extraction.existingEmi ?: profile.existingEmi
        )
    }
}

