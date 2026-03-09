package com.example.loaneligibilityindiaandroid.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.loaneligibilityindiaandroid.models.EmploymentType
import com.example.loaneligibilityindiaandroid.models.LoanDocType
import com.example.loaneligibilityindiaandroid.viewmodel.EligibilityViewModel
import java.text.NumberFormat
import java.util.Locale

@Composable
fun LoanEligibilityScreen(viewModel: EligibilityViewModel) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.onDocumentPicked(context, it) }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Loan Eligibility AI (Google Play)", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Applicant Profile", fontWeight = FontWeight.SemiBold)

                OutlinedTextField(
                    value = viewModel.profile.fullName,
                    onValueChange = viewModel::setFullName,
                    label = { Text("Full Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    EmploymentType.entries.forEach { type ->
                        FilterChip(
                            selected = viewModel.profile.employmentType == type,
                            onClick = { viewModel.setEmploymentType(type) },
                            label = { Text(type.label) }
                        )
                    }
                }

                NumberField("Monthly Net Income", viewModel.profile.monthlyNetIncome.toLong().toString(), viewModel::setMonthlyIncome)
                NumberField("Annual ITR Income", viewModel.profile.annualITRIncome.toLong().toString(), viewModel::setAnnualItrIncome)
                NumberField("Existing EMI", viewModel.profile.existingEmi.toLong().toString(), viewModel::setExistingEmi)
                NumberField("Property Value", viewModel.profile.propertyValue.toLong().toString(), viewModel::setPropertyValue)
                NumberField("Requested Loan Amount", viewModel.profile.requestedLoanAmount.toLong().toString(), viewModel::setRequestedAmount)

                Text("CIBIL: ${viewModel.profile.cibilScore}")
                Slider(
                    value = viewModel.profile.cibilScore.toFloat(),
                    onValueChange = { viewModel.setCibil(it.toInt()) },
                    valueRange = 300f..900f
                )

                Text("Preferred Tenure: ${viewModel.profile.preferredTenureYears} years")
                Slider(
                    value = viewModel.profile.preferredTenureYears.toFloat(),
                    onValueChange = { viewModel.setTenure(it.toInt().coerceIn(5, 30)) },
                    valueRange = 5f..30f
                )
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Calculation Source", fontWeight = FontWeight.SemiBold)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Use Live Bank Policies API", modifier = Modifier.weight(1f))
                    Switch(checked = viewModel.useLiveBankPolicies, onCheckedChange = { viewModel.useLiveBankPolicies = it })
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Use Live Eligibility API", modifier = Modifier.weight(1f))
                    Switch(checked = viewModel.useLiveEligibilityApi, onCheckedChange = { viewModel.useLiveEligibilityApi = it })
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Documents", fontWeight = FontWeight.SemiBold)

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    LoanDocType.entries.forEach { type ->
                        FilterChip(
                            selected = viewModel.selectedDocType == type,
                            onClick = { viewModel.selectedDocType = type },
                            label = { Text(type.label) }
                        )
                    }
                }

                Button(onClick = { picker.launch(arrayOf("application/pdf", "image/*")) }) {
                    Text("Upload ${viewModel.selectedDocType.label}")
                }

                if (viewModel.isProcessingDocument) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.width(18.dp).height(18.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Processing OCR...")
                    }
                }

                viewModel.documents.forEach { doc ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(doc.name, fontWeight = FontWeight.Medium)
                            Text(doc.type.label, style = MaterialTheme.typography.bodySmall)
                            doc.ocrSummary?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                            doc.ocrConfidence?.let { Text("OCR Confidence: ${(it * 100).toInt()}%", style = MaterialTheme.typography.bodySmall) }
                            TextButton(onClick = { viewModel.removeDocument(doc.id) }) { Text("Remove") }
                        }
                    }
                }
            }
        }

        Button(
            onClick = { viewModel.calculateEligibility() },
            enabled = !viewModel.isCalculating && !viewModel.isProcessingDocument,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (viewModel.isCalculating) {
                CircularProgressIndicator(modifier = Modifier.width(18.dp).height(18.dp), strokeWidth = 2.dp)
            } else {
                Text("AI Calculate Loan Eligibility")
            }
        }

        if (viewModel.statusMessage.isNotBlank()) {
            Text(viewModel.statusMessage, style = MaterialTheme.typography.bodySmall)
        }

        if (viewModel.aiSummary.isNotBlank()) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp)) {
                    Text("AI Summary", fontWeight = FontWeight.SemiBold)
                    Text(viewModel.aiSummary)
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Bank Eligibility Results", fontWeight = FontWeight.SemiBold)
                if (viewModel.results.isEmpty()) {
                    Text("Run calculation to view results")
                } else {
                    viewModel.results.forEach { result ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(result.bankName, fontWeight = FontWeight.Medium)
                                Text(if (result.isEligible) "Likely Eligible" else "Needs Improvement")
                                Text("Eligible Amount: ${formatInr(result.eligibleAmount)}")
                                Text("Expected EMI: ${formatInr(result.expectedEmi)} at ${"%.2f".format(result.recommendedRate)}%")
                                Text("Confidence: ${(result.confidence * 100).toInt()}%")
                                result.notes.forEach { Text("| $it", style = MaterialTheme.typography.bodySmall) }
                            }
                        }
                    }
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("EMI Simulator", fontWeight = FontWeight.SemiBold)
                Text("Loan Amount: ${formatInr(viewModel.simulationPrincipal)}")
                Slider(value = viewModel.simulationPrincipal.toFloat(), onValueChange = { viewModel.simulationPrincipal = it.toDouble() }, valueRange = 500000f..30000000f)

                Text("Interest Rate: ${"%.2f".format(viewModel.simulationRate)}%")
                Slider(value = viewModel.simulationRate.toFloat(), onValueChange = { viewModel.simulationRate = it.toDouble() }, valueRange = 7f..14f)

                Text("Tenure: ${viewModel.simulationTenureYears} years")
                Slider(value = viewModel.simulationTenureYears.toFloat(), onValueChange = { viewModel.simulationTenureYears = it.toInt().coerceIn(5, 30) }, valueRange = 5f..30f)

                Text("Estimated EMI: ${formatInr(viewModel.simulatedEmi)} / month", fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun NumberField(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )
}

private fun formatInr(value: Double): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    return formatter.format(value)
}


