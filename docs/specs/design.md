# Colombian Payments - Design

A payment fee calculator for Colombian payments, built around the **Decorator** pattern: a base `Payment`
is wrapped by one decorator per charge or credit (VAT, gateway fee, withholding, GMF, cashback). Each layer adds
a line to the breakdown and changes what the payer pays or what the merchant receives.
Backend (`backend/`) and frontend (`frontend/`) are independent: they only share the contract below. Do not change it.
All code, comments, identifiers, UI text and commit messages are in English. No co-author trailers in commits.

## Stack
- Backend: Java 21, Spring Boot 3.3, Maven, hexagonal layout under `com.colombianpayments`, JUnit 5 + MockMvc, jjwt.
  Already in place: JWT issuer and filter, CORS, `ApiExceptionHandler` (400 `VALIDATION`, 401 `UNAUTHORIZED`), `AuthController`
  (`demo` / `demo123`, token valid 24h), `@SafeText`. Keep them, build the rest.
- Frontend: Vite + React + TypeScript, Tailwind CSS v4. No other UI libs.

## Domain (backend)
`Payment` (interface): `baseAmountCop()`, `payerTotalCop()`, `merchantNetCop()`, `layers()` (list of `Layer`), `description()`.
`BasePayment` holds the purchase amount. `PaymentDecorator` (abstract) wraps a `Payment` by composition and adds one layer.
`Layer(code, label, amountCop, bearer, notes)`: `bearer` is `PAYER` (adds to what the payer pays when amount > 0, reduces it when < 0)
or `MERCHANT` (amount is always negative: it reduces what the merchant receives).
`payerTotalCop = baseAmountCop + sum(PAYER layers)`; `merchantNetCop = baseAmountCop + sum(MERCHANT layers)`.
Money is `long` COP, rounding half up.

### Option codes and rules (wrapping order, innermost to outermost, fixed)
1. `VAT` (IVA): `+19%` of the base amount, bearer PAYER.
2. `GATEWAY_FEE`: `-(2.65% of the base amount + 900)`, bearer MERCHANT.
3. `WITHHOLDING` (retefuente): `-2.5%` of the base amount, bearer MERCHANT, applies only when base >= 27 UVT
   (UVT is configurable as `payments.uvt-cop`, default 52374, a demo value). If the base is below the threshold the option is
   rejected with 400 `WITHHOLDING applies from 27 UVT (<threshold> COP)`.
4. `GMF` (4x1000): `+0.4%` of the payer total accumulated by the inner layers (base + VAT), bearer PAYER. Order dependent on purpose.
5. `CASHBACK`: `-1%` of the base amount, capped at 20000 COP, bearer PAYER (a credit to the payer, does not change GMF).
No incompatible pairs. Duplicate codes are ignored. Order of the request does not matter.
Worked example (use it as a test): base 2000000 with all five options:
VAT +380000, GATEWAY_FEE -53900, WITHHOLDING -50000, GMF +9520 (0.4% of 2380000), CASHBACK -20000
=> payerTotal 2369520, merchantNet 1896100.

### Custom annotations
- `@ValidNit`: Colombian NIT check. Format `123456789-0` or digits only with the check digit; the check digit is computed with
  the DIAN weights `3,7,13,17,19,23,29,37,41,43,47,53,59,67,71` applied to the digits from right to left, `r = sum % 11`,
  `dv = r in (0,1) ? r : 11 - r`. Reject anything that does not match. Use on `merchantNit`.
- `@Idempotent`: marks the controller method that creates a payment. An interceptor (or aspect) reads the `Idempotency-Key`
  header (required, 8-64 chars, otherwise 400). First call runs and the response is stored in memory by key. A repeated call with
  the same key and same body returns the stored response with header `Idempotent-Replayed: true` and creates nothing new;
  same key with a different body is 409 `IDEMPOTENCY_CONFLICT`.
- `@SafeText` already exists; use it on free text fields.

## Contract
Base URL `http://localhost:8080`, CORS for `http://localhost:5173`. Everything except login needs `Authorization: Bearer <token>`.
- `POST /api/v1/auth/login` `{username,password}` -> `{token,expiresAt}` (exists).
- `GET /api/v1/options` -> `[{code,name,description,pricingRule}]` for the five options, in wrapping order.
- `POST /api/v1/payments/quote` `{amountCop,merchantNit,description?,options:[code]}` ->
  `{currency:"COP",baseAmountCop,payerTotalCop,merchantNetCop,layers:[{code,label,amountCop,bearer,notes:[string]}]}`
  `layers` is ordered innermost to outermost. Validation: `amountCop` between 1000 and 100000000, valid NIT, known options.
- `POST /api/v1/payments` same body plus required header `Idempotency-Key` -> 201 `{id,status:"APPROVED",createdAt,quote:{...same as above}}`.
- `GET /api/v1/payments` -> list of created payments, newest first.
- Errors: 400 `{error:"VALIDATION",message}`, 401 `{error:"UNAUTHORIZED",message}`, 409 `{error:"IDEMPOTENCY_CONFLICT",message}`.

## Backend tests (`mvn test` must pass)
One test class per decorator, the worked example above, order independence of the request, each 400 rule, NIT validator
(valid and invalid cases, e.g. `900373913-1` valid, `900373913-2` invalid), idempotency (replay, conflict, missing key),
MockMvc for login, 401, options, quote, payments and CORS preflight.

## Frontend (Vite + React + TS + Tailwind v4)
Two-column layout, stacked on mobile. Formal fintech look: ink blue `#0b2545`, white, accent green `#13a06f`, warning amber.
- Left: payment form: amount (COP, formatted live), merchant NIT with live check-digit validation (same algorithm as the backend),
  description, option toggles with the rule shown, a "Pay" button, and a "Retry same payment" button that resends the SAME
  `Idempotency-Key` to show the replay (display a badge "replayed, nothing charged twice").
- Right: live quote (debounced 300 ms) as a stack of layers from the base outward, one card per layer colored by bearer
  (payer / merchant), showing the amount with sign, plus two totals: "Payer pays" and "Merchant receives".
- Below: payments history table from `GET /api/v1/payments`.
- Auto sign-in with `demo/demo123`, token in memory, re-login once on 401. API base from `VITE_API_URL` (default `http://localhost:8080`).
- Loading, error and empty states. Keyboard accessible. `npm run build` must pass with no TypeScript errors.
- Mock mode is NOT required.
