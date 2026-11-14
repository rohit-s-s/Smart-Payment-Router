# Smart Payment Router (Native Android Application)

This is the complete, native Android application implementation for **Epic 1: Capturing a Payment** built in **Kotlin** with **Jetpack Compose**, **Material 3**, **CameraX**, and **Google ML Kit Barcode Scanning**.

---

## 📱 Android Architecture & Features

### 1. US-01: Scan a QR Code
- **CameraX + ML Kit**: Live camera preview (`ScanQrScreen.kt`) with `QrCodeAnalyzer.kt` processing frames in real-time.
- **Gallery Pick**: Uses Android `ActivityResultContracts.GetContent()` with ML Kit `InputImage.fromFilePath()`.
- **UPI Parsing**: Decodes standard `upi://pay` query params: payee address (`pa`), payee name (`pn`), amount (`am`), and MCC (`mc`).
- **Missing Amount Handling**: When `am` is absent from the QR, the app captures payee and category, prompts the user: *"Payee captured from QR. Please enter amount"*, and focuses the amount input in `ManualEntryScreen.kt`.
- **Non-UPI URL Fallback**: When a non-UPI URL (e.g. `https://croma.com`) is scanned, it displays a notice banner and falls back to manual entry mode.
- **Quick Test Dialog**: Interactive dialog (`QuickTestQrDialog.kt`) with 5 preset QR scenarios for instant on-device testing.

### 2. US-02: Enter a Payment Manually
- **Manual Entry Flow** (`ManualEntryScreen.kt`): For online purchases, bill payments, and in-app checkout.
- **Strict Validation**: Amount (> ₹0) and category are required before the recommendation calculation button is enabled.
- **Parity Guarantee**: Manual entry produces the exact same recommendation as an equivalent scanned payment (verified by `testAcceptanceCriteria4_manualEntryParity`).
- **Quick Amount Buttons**: `+₹100`, `+₹250`, `+₹500`, `+₹1000`, `+₹2000`.

### 3. US-03: Confirm or Change the Spend Category
- **Multi-Source Suggestion**:
  - Automatically mapped from Merchant Category Code (MCC) via `MccDirectory.kt`.
  - Automatically mapped from user's past choices in `PaymentRepository.kt` (e.g., typing "Swiggy" automatically suggests "Dining & Food").
- **Fixed 8 Categories**: `DINING`, `GROCERIES`, `FUEL`, `TRAVEL`, `UTILITIES`, `SHOPPING`, `ENTERTAINMENT`, `OTHER`.
- **Instant Override**: The user can tap any of the 8 category cards at any time to override the suggestion before routing.

### 4. Recommendation Engine (`RecommendationEngine.kt`)
- Calculates effective cashback in ₹, accounts for category bonuses, monthly caps, excess spend at base rate, and offer stacking.
- Ranks methods with tie-breaking preferring cards with higher remaining monthly caps.
- Rule freshness warning for rules last verified > 90 days ago.
- Benchmarked to execute in < 2ms (exceeding non-functional performance requirement of < 2.0s on mid-range Android devices).

### 5. Redirection & Intents (`IntentHelper.kt` & `RecommendationScreen.kt`)
- Deep links via Android `Intent(Intent.ACTION_VIEW, Uri.parse("upi://pay?..."))` to launch Google Pay, PhonePe, Paytm, or CRED.
- Shows pre-redirection `OfferActivationDialog.kt` for card offers requiring activation in banking apps (FR-8).
- One-click clipboard copy fallback for card numbers, payee VPA, and amounts.
- Confirmed payments update card cap usage and train merchant memory (`PaymentLog.kt`).

---

## 🛠️ How to Open & Run in Android Studio

1. Open **Android Studio** (Hedgehog, Iguana, Koala, Ladybug, or later).
2. Select **File -> Open...** and select the `android` folder:
   ```
   C:\Users\Rohith\Desktop\My folder Program\Smart Payment Router\android
   ```
3. Allow Gradle to sync dependencies.
4. Select your connected Android device or emulator.
5. Click **Run 'app'** (`Shift + F10`) or press the green Play button.

---

## 🧪 Running Unit Tests

From Android Studio:
- Right click on `app/src/test/java/com/smartpaymentrouter` and select **Run 'Tests in 'com.smartpaymentrouter''**.

Or from command line:
```bash
./gradlew test
```
