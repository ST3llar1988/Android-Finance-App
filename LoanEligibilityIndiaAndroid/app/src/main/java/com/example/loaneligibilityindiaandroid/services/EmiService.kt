package com.example.loaneligibilityindiaandroid.services

import kotlin.math.pow

object EmiService {
    fun calculateEmi(principal: Double, annualRate: Double, tenureYears: Int): Double {
        val monthlyRate = annualRate / (12 * 100)
        val months = tenureYears * 12.0

        if (principal <= 0 || annualRate <= 0 || months <= 0) return 0.0

        val factor = (1 + monthlyRate).pow(months)
        return principal * monthlyRate * factor / (factor - 1)
    }

    fun maxPrincipalForAffordableEmi(maxEmi: Double, annualRate: Double, tenureYears: Int): Double {
        val monthlyRate = annualRate / (12 * 100)
        val months = tenureYears * 12.0

        if (maxEmi <= 0 || annualRate <= 0 || months <= 0) return 0.0

        val factor = (1 + monthlyRate).pow(months)
        return maxEmi * ((factor - 1) / (monthlyRate * factor))
    }
}

