<<<<<<< HEAD
# Smart Payment Router (Epic 1: Capturing a Payment)

An intelligent payment router web application that helps users maximize rewards and cashback for every transaction by analyzing their cards, UPI apps, reward caps, and active offers at the moment of payment.

Built based on **Epic 1: Capturing a Payment** (`User_Stories/epic1.txt`) and **Smart Payment Router Requirements Draft v1** (`smart_payment_router_requirements.md`).

---

## 🚀 Features Implemented for Epic 1

### 1. US-01: Scan a QR Code
- **Camera Scanning**: Live video camera viewfinder with scan reticle, laser animation, torch/flashlight toggle, and front/rear camera switcher.
- **Gallery / File Picker**: Pick any QR code image (PNG, JPG, WebP) directly from device storage or drag & drop onto the scanner.
- **UPI URI Decoding**: Decodes standard `upi://pay` URI parameters:
  - Payee address / VPA (`pa`)
  - Payee name (`pn`)
  - Fixed amount (`am`), if present
  - Merchant Category Code (`mc`)
- **Missing Amount Handling**: If the QR has no fixed amount, the app asks the user to enter the amount before producing recommendations.
- **Non-UPI Fallback**: If the QR is not a UPI string (e.g. a website link like `https://croma.com`), the app switches to manual entry mode and prompts for amount and spend category.
- **1-Click Test Scenarios**: Includes preset test QRs for instant verification without needing a physical printed code.

### 2. US-02: Enter a Payment Manually
- **First-Class Manual Path**: Dedicated manual entry flow for online shopping, bills, and in-app purchases.
- **Strict Validation**: Amount and category are required before recommendations are produced.
- **Parity Guarantee**: Manually entered purchases receive the exact same recommendation and reward calculation as an equivalent scanned purchase.
- **Quick-Add Buttons**: Accessible `+₹100`, `+₹250`, `+₹500`, `+₹1000`, `+₹2000` buttons.

### 3. US-03: Confirm or Change the Spend Category
- **Multi-Source Category Suggestion**:
  - Automatically mapped from Merchant Category Code (MCC) when scanning (e.g., MCC `5812` -> `dining`, `5541` -> `fuel`, `5411` -> `groceries`, `4900` -> `utilities`).
  - Automatically remembered from user's past choices and saved merchant memory (e.g., typing "Swiggy" automatically suggests "Dining & Food").
  - Clear source badge indicates how the category was derived.
- **Fixed 8 Categories**:
  - `dining` (Restaurants, cafes, food delivery)
  - `groceries` (Supermarkets, daily essentials)
  - `fuel` (Petrol pumps, EV charging)
  - `travel` (Flights, trains, cabs, hotels)
  - `utilities` (Electricity, water, gas, recharge)
  - `shopping` (E-commerce, apparel, electronics)
  - `entertainment` (Movies, events, streaming)
  - `other` (General miscellaneous spend)
- **Instant Category Override**: Users can tap any category at any time to override the suggestion.

---

## ⚡ Non-Functional Requirements & Performance

| Requirement | Implementation & Verification | Status |
| :--- | :--- | :--- |
| **Performance** | Recommendation computed in **< 1ms** in browser memory (requirement: < 2000ms). | ✅ Verified |
| **Offline Use** | Zero network dependencies at runtime. Rules, cards, offers, and merchant history stored locally in `localStorage`. | ✅ 100% Offline |
| **Accessibility** | ARIA labels on viewfinder, screen-reader accessible forms, high-contrast badges, keyboard navigation. | ✅ Accessible |
| **Accuracy & Transparency**| Every recommendation displays its exact reason, breakdown, and estimated reward in Indian Rupees (`₹`). | ✅ Fully Transparent |

---

## 💳 Recommendation Engine & Edge Cases

- **Cap Overflow Handling**: When a transaction exceeds a card's monthly reward cap, the bonus drops to the card's base rate for the excess amount.
- **Offer Stacking vs Non-Stacking**: Selects all stackable offers or the highest-value non-stackable offer, with transparent explanations.
- **Tie-Breaking**: If two cards provide identical cashback, the engine prioritizes the card with the higher remaining monthly cap.
- **Rule Verification Alert**: Warns users if card reward rules were last confirmed >90 days ago.
- **Redirection & Fallback**:
  - UPI deep link generation (`upi://pay?pa=...&am=...&cu=INR&mc=...`).
  - Pre-redirection offer activation reminder (for offers requiring banking app activation).
  - One-click clipboard copy fallback for card numbers, payee VPA, and amounts.
- **Payment Log (FR-9)**: Tracks cumulative savings, average cashback percentage, and capture method.

---

## 🛠️ Tech Stack

- **Frontend**: React 18, TypeScript, Tailwind CSS, Lucide Icons
- **QR Decoding**: jsQR (pure JavaScript canvas scanner, works offline)
- **Build Tool**: Vite
- **Testing**: Vitest (13/13 unit and acceptance tests passing)

---

## 🧪 Running Tests & Development

```bash
# Run unit & acceptance test suite
npm run test

# Start development server
npm run dev

# Build production bundle
npm run build
```
