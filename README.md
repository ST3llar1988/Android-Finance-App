# Loan Eligibility India Android (Google Play)

Android version of the loan eligibility app for India, built with Kotlin + Jetpack Compose.

## Features
- Upload salary slip / ITR (PDF or image)
- OCR extraction via Google ML Kit
- Auto-prefill profile fields from extracted text
- AI-style loan eligibility calculation across Indian banks
- EMI simulator
- Optional live API toggles:
  - Live bank policies
  - Live eligibility calculation

## Project Path
- `LoanEligibilityIndiaAndroid/`

## Run in Android Studio
1. Open Android Studio.
2. Open the folder `LoanEligibilityIndiaAndroid`.
3. Let Gradle sync.
4. Run on emulator or Android device (API 26+).

## Google Play Readiness Notes
- Add privacy policy URL in Play Console.
- Add a disclaimer that eligibility is indicative, not sanction approval.
- Add proper app icon, screenshots, and data safety details.

## Backend Contract
- `LoanEligibilityIndiaAndroid/api/openapi.yaml`

## Important
OCR extraction rules are heuristic. For production, add stronger document parsing and manual correction UI for extracted values.

