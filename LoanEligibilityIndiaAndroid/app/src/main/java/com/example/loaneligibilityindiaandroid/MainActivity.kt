package com.example.loaneligibilityindiaandroid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.example.loaneligibilityindiaandroid.ui.LoanEligibilityScreen
import com.example.loaneligibilityindiaandroid.viewmodel.EligibilityViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: EligibilityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MaterialTheme {
                Surface {
                    LoanEligibilityScreen(viewModel = viewModel)
                }
            }
        }
    }
}

