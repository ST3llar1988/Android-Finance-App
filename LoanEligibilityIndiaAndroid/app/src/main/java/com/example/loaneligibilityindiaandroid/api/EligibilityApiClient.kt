package com.example.loaneligibilityindiaandroid.api

import com.example.loaneligibilityindiaandroid.models.ApplicantProfile
import com.example.loaneligibilityindiaandroid.models.BankPolicy
import com.example.loaneligibilityindiaandroid.models.EligibilityResult
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

class EligibilityApiClient(
    private val config: ApiConfig = ApiConfig(),
    private val client: OkHttpClient = OkHttpClient()
) {
    fun fetchBankPolicies(): List<BankPolicy> {
        val requestBuilder = Request.Builder()
            .url("${config.baseUrl}/v1/banks/policies")
            .get()
            .addHeader("Accept", "application/json")

        config.apiKey?.takeIf { it.isNotBlank() }?.let { requestBuilder.addHeader("Authorization", "Bearer $it") }

        client.newCall(requestBuilder.build()).execute().use { response ->
            if (!response.isSuccessful) error("Policy API failed with ${response.code}")
            val body = response.body?.string().orEmpty()
            val array = JSONArray(body)

            return buildList {
                for (i in 0 until array.length()) {
                    val item = array.getJSONObject(i)
                    add(
                        bankPolicyFromJson(
                            name = item.optString("name"),
                            baseRate = item.optDouble("baseRate"),
                            maxTenureYears = item.optInt("maxTenureYears"),
                            maxLtv = item.optDouble("maxLTV"),
                            maxFoir = item.optDouble("maxFOIR"),
                            minCibil = item.optInt("minCIBIL")
                        )
                    )
                }
            }
        }
    }

    fun calculateEligibility(profile: ApplicantProfile): List<EligibilityResult> {
        val payload = JSONObject(profile.toRequestMap()).toString()
        val body = payload.toRequestBody("application/json".toMediaType())

        val requestBuilder = Request.Builder()
            .url("${config.baseUrl}/v1/eligibility/calculate")
            .post(body)
            .addHeader("Accept", "application/json")
            .addHeader("Content-Type", "application/json")

        config.apiKey?.takeIf { it.isNotBlank() }?.let { requestBuilder.addHeader("Authorization", "Bearer $it") }

        client.newCall(requestBuilder.build()).execute().use { response ->
            if (!response.isSuccessful) error("Eligibility API failed with ${response.code}")
            val responseBody = response.body?.string().orEmpty()
            val root = JSONObject(responseBody)
            val items = root.optJSONArray("results") ?: JSONArray()

            return buildList {
                for (i in 0 until items.length()) {
                    val item = items.getJSONObject(i)
                    val notesJson = item.optJSONArray("notes") ?: JSONArray()
                    val notes = buildList {
                        for (idx in 0 until notesJson.length()) add(notesJson.optString(idx))
                    }

                    add(
                        eligibilityResultFromJson(
                            bankName = item.optString("bankName"),
                            eligibleAmount = item.optDouble("eligibleAmount"),
                            requestedAmount = item.optDouble("requestedAmount"),
                            expectedEmi = item.optDouble("expectedEMI"),
                            recommendedRate = item.optDouble("recommendedRate"),
                            confidence = item.optDouble("confidence"),
                            notes = notes
                        )
                    )
                }
            }
        }
    }
}

