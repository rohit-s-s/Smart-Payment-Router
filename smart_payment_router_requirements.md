# Smart Payment Router: Requirements

Saturday, November 14, 2026 · Status: Draft v1

## 1. Purpose

The app helps a user pick the payment method that gives the highest reward or cashback for a given purchase. The user supplies the purchase type and amount, either by scanning a UPI or merchant QR code or by typing them in. The app then compares the user's own cards, UPI apps and offers, recommends the best option, and sends the user to that app to pay. Manual entry is a first-class path so the product works for online and in-app purchases, which QR scanning cannot cover.

## 2. Problem Statement

Users hold several credit cards, debit cards and UPI apps, each with different cashback rates, category bonuses, caps and offers. Most people pay with whichever method comes to mind, and so leave rewards unclaimed. Reward rules are spread across card portals, emails and fine print, and they change often. The user needs one place that knows their rules and makes the decision at the moment of payment.

## 3. Goals and Success Measures

The main goal is to maximise the reward on each eligible transaction without adding friction to payment. Success is measured by the share of tracked transactions where the recommended method was used, the average reward per transaction compared with a user's usual method, the number of manual entries versus QR scans (to confirm the manual path is used), and the time from opening the app to redirecting, which should stay under five seconds.

## 4. Scope

In scope: QR scanning (UPI and merchant QR), manual entry of amount and spend category, a user-maintained inventory of cards and UPI apps, a user-maintained list of offers, a recommendation engine, redirection to the chosen payment app, and a log of completed payments.

Out of scope for v1: site-specific or merchant-specific offers fetched from the internet, automatic reading of bank or card statements, actually making payments inside this app, and any storage of full card numbers, CVVs or UPI PINs.

## 5. Users and Assumptions

The primary user is an individual with two or more cards or UPI apps who wants to optimise rewards without tracking rules manually. We assume the user will enter their own card reward rules and offers, because these are not available through a public API. We assume that the payment app accepts a deep link or intent with a payee, amount and category. Where a payment app does not support this, the app will show the recommendation and the user completes the payment in that app themselves.

## 6. Functional Requirements

**FR-1, QR capture.** The user can scan a QR code with the device camera or pick one from the gallery. The app decodes the UPI payment string if present, reading the payee address, the amount if fixed, and the merchant category code if present. If the QR has no amount, the app asks for it.

**FR-2, Manual entry.** The user can enter the amount, a spend category and an optional merchant name without scanning. This path must support online shopping, bills, travel bookings and in-app purchases. The amount and category fields are required before a recommendation is produced.

**FR-3, Category selection.** The app maps the payment to one of a fixed set of spend categories such as dining, groceries, fuel, travel, utilities, shopping, entertainment, and other. The category comes from the merchant category code when scanning, from the user's selection when typing, or from a merchant-name lookup against the user's saved merchants. The user can always override the category.

**FR-4, Payment method inventory.** The user can add credit cards, debit cards and UPI apps, each with a name, issuer, network, and reward rules. Reward rules include a base rate, category-specific rates, monthly or per-transaction caps, minimum spend, and an exclusion list. The user can edit or remove any method. Only the issuer, a display name and the reward rules are stored, never the full card number, expiry or CVV.

**FR-5, Offer management.** The user can add an offer manually. Each offer has a title, the payment method it applies to, the categories or minimum amount it needs, the discount or cashback it gives, a start and end date, a per-user or total usage cap, and whether it stacks with other offers. The user can mark an offer as used, and the app stops recommending it after the cap is reached.

**FR-6, Recommendation engine.** For a given amount and category, the engine calculates the effective reward for each eligible method: the base or category rate, applied up to the remaining cap, plus any eligible offer, minus any fee. It ranks the methods by effective reward and shows the top choice with the reason and a short runner-up list. If two methods give the same reward, the app prefers the one with the higher remaining cap.

**FR-7, Redirection.** After the user confirms the recommendation, the app opens the chosen payment app or card portal with the amount and payee pre-filled where the app supports it. If the app cannot be opened or pre-filled, the app copies the amount and payee to the clipboard and shows the instructions to complete the payment.

**FR-8, Offer applied reminder.** Before redirecting, the app shows the offer that will be applied and asks the user to confirm they have activated it in their card app, since some offers need activation.

**FR-9, Payment log.** After the user returns to the app, the app asks them to confirm which method they used and the final amount paid. The log stores the date, amount, category, method, and reward expected. The log is used to show actual rewards earned against the recommendation.

**FR-10, Expiry and cap alerts.** The app notifies the user when an offer is about to expire or when a method is close to its reward cap for the month.

**FR-11, Data updates.** The user can review and update reward rules at any time. The app shows the date when each method's rules were last confirmed and prompts the user to re-check rules that are older than a set period, such as 90 days.

## 7. Non-Functional Requirements

**Privacy.** All card and offer data is stored on the device by default. Any sync or cloud backup must be opt-in and encrypted. The app must not request bank login credentials.

**Security.** The app stores no payment credentials. Access to the app may be protected by device biometrics. Log entries are encrypted at rest.

**Performance.** A recommendation is shown within two seconds of the amount and category being known, on a mid-range Android device.

**Accuracy and transparency.** Every recommendation shows the reason and the reward value in rupees. Estimated rewards are labelled as estimates, since issuers may change caps or rates without notice.

**Reliability.** The app works offline for recommendations, using stored rules. Redirection requires a network connection only where the payment app itself needs one.

**Accessibility.** The screens follow platform accessibility guidelines, including screen reader labels on the scanner and the amount input.

**Compliance.** The app must not present itself as a bank or payment provider and must not process payments. Reward figures are shown as user-entered information, with a clear disclaimer.

## 8. Data Model Summary

PaymentMethod holds id, type (credit card, debit card or UPI app), issuer, display name, network, and reward rules. RewardRule holds method id, category, rate, cap period, cap amount, minimum spend, and exclusions. Offer holds id, method id, title, eligibility, benefit, validity dates, usage cap, usage count, and stacking flag. Transaction holds id, timestamp, amount, category, merchant name (optional), source (scan or manual), recommended method, chosen method, expected reward, and actual reward if entered.

## 9. Edge Cases

- A QR code with no amount: the app asks for the amount.
- A QR code that is not a UPI string (for example, a website link): the app treats it as a manual-entry case and asks for the amount and category.
- A payment that exceeds a card's cap: the engine drops the card's bonus to its base rate for the excess.
- Two offers that do not stack: the engine applies only the higher-value offer and explains this.
- A method whose rules are out of date: the recommendation is shown with a warning.
- No method is eligible for a category: the engine falls back to the method with the highest base rate and says so.
- The user pays by a different method than recommended: the log records both, and the app does not treat it as an error.

## 10. Site-Specific and Online Offers (Out of Scope for v1)

This requirement excludes offers tied to specific websites or merchants that would need to be fetched online. For reference, there are three realistic ways to get that data in future versions.

The first is partnership or affiliate data feeds from cashback networks, card issuers or large merchants. These give reliable and structured data but require commercial agreements.

The second is user-contributed offers, where users add the offers they see on a merchant's page. The app already supports this through FR-5.

The third is web scraping of merchant or aggregator pages. This is fragile, can break when pages change, and may conflict with a site's terms of use, so it should not be used without legal review.

Public APIs for card-specific reward rules are generally not available, so card rules will continue to come from the user.

## 11. Constraints on Redirection

UPI deep links accept a payee address, amount and merchant category code, but individual apps decide whether to honour these fields. Redirection to card apps and bank apps varies by platform. iOS limits how one app can open another, so on iOS the fallback of copying details and showing instructions may be the common path. The team must test redirection against each target app before release and maintain a list of which apps support full pre-fill.

## 12. Open Questions

1. Which spend categories should be the fixed set for v1, and should they follow a standard merchant category code list?
2. Should the app support multiple currencies or only Indian rupees?
3. How much manual rule entry will users accept, and should the app offer starter templates for popular cards?
4. Should the payment log be required or optional, given it is the only source of actual reward data?
5. Which platforms are in the first release: Android only, or Android and iOS?

## 13. Milestones

**Phase 1:** Data model, payment method and offer management, manual entry, and the recommendation engine. Users test the rules against their own past transactions.

**Phase 2:** QR scanning and UPI string parsing, redirection with clipboard fallback, and the payment log.

**Phase 3:** Expiry and cap alerts, reward reporting, and a review of whether any site-specific data source is worth adding.

## 14. Acceptance Criteria

- Given a stored card with a 5 percent dining rate and a monthly cap of 1,000 rupees, and a dining payment of 500 rupees with no offers, the app recommends that card and shows an estimated reward of 25 rupees.
- Given the same card with 900 rupees already used this month, the app shows the reduced reward for the remaining 100 rupees of cap and recommends the next-best method if its reward is higher.
- Given a scanned UPI QR with an amount and a merchant category code, the app pre-fills the amount and category without the user typing them.
- Given a manually entered online purchase with no QR, the app produces the same recommendation as for the equivalent scanned purchase.
