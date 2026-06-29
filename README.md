
# 💳 Payment Integration System — PayPal + Stripe + Strategy Pattern

> A clean, extensible Spring Boot payment backend using the **Strategy Pattern** with PayPal and Stripe REST API integrations.

---

## 📌 Overview

This project demonstrates how to:

- Integrate with **PayPal** using direct REST APIs (no SDK)
- Integrate with **Stripe** using official Java SDK
- Apply the **Strategy Pattern** to support multiple payment providers (PayPal, Stripe)
- Build a clean, extensible architecture for future payment gateways

---

## 🧠 Key Concepts

### 🔹 Strategy Pattern

The system uses the Strategy Pattern to dynamically select the payment provider at runtime.

```
Controller → PaymentService → StrategyFactory → PaymentStrategy (PayPal / Stripe)
```

Each payment provider implements a common interface:

```java
public interface PaymentStrategy {
    PaymentCreateResponse createPayment(PaymentRequest request);
    PaymentCaptureResponse capturePayment(String paymentId);
}
```

**Strategies:**
- `PayPalStrategy` — `@Component("PAYPAL")`
- `StripeCheckoutStrategy` — `@Component("STRIPE_CHECKOUT")`
- `StripeIntentStrategy` — `@Component("STRIPE_INTENT")`

---

## ⚙️ Technologies Used

| Technology | Purpose |
|---|---|
| Java 17+ | Core language |
| Spring Boot | Application framework |
| Spring WebClient (WebFlux) | Reactive HTTP client (PayPal) |
| Stripe Java SDK | Synchronous SDK (Stripe) |
| PayPal REST API (Sandbox) | Payment provider |
| Stripe API | Payment provider |
| Lombok | Boilerplate reduction |
| PostgreSQL / H2 | Database (PostgreSQL prod, H2 test) |

---

## 📦 API Endpoints

### 🔹 1. Create Payment

```http
POST /api/payments/{provider}/create
```

**Supported providers:** `PAYPAL`, `STRIPE_CHECKOUT`, `STRIPE_INTENT`

**Example - PayPal:**

```http
POST /api/payments/paypal/create
```

**Example - Stripe Checkout:**

```http
POST /api/payments/STRIPE_CHECKOUT/create
```

**Example - Stripe Intent:**

```http
POST /api/payments/STRIPE_INTENT/create
```

**Request Body:**

```json
{
  "amount": "10.00",
  "currency": "USD"
}
```

**PayPal Response:**

```json
{
  "paymentId": "ORDER_ID",
  "approvalUrl": "https://www.sandbox.paypal.com/checkoutnow?token=..."
}
```

**Stripe Response:**

```json
{
  "paymentId": "pi_3TKzoKRsP4kJBEgL0xLhHv6p",
  "approvalUrl": "pi_3TKzoKRsP4kJBEgL0xLhHv6p_secret_Q1V4UZylWwwmwQl6tgZCGxYX6"
}
```

---

### 🔹 2. Capture Payment

**PayPal:**
```http
POST /api/payments/paypal/capture/{orderId}
```

**Stripe Checkout:**
```http
POST /api/payments/STRIPE_CHECKOUT/capture/{sessionId}
```

**Stripe Intent:**
```http
POST /api/payments/STRIPE_INTENT/capture/{paymentIntentId}
```

**Response:**
```json
{
  "paymentId": "PAYMENT_ID",
  "status": "COMPLETED" // or "succeeded" for Stripe
}
```

---

### 🔹 3. Cancel Payment

```http
GET /api/payments/paypal/cancel
```

---

## 🧪 How to Test PayPal Integration

### ✅ Step 1 — Create Payment (Postman)

```http
POST http://localhost:8080/api/payments/paypal/create
```

Copy the `approvalUrl` from the response.

---

### ✅ Step 2 — Approve Payment (Browser)

1. Open the `approvalUrl` in your browser
2. Log in using your **PayPal Sandbox Buyer Account**
3. Click **Approve**

---

### ✅ Step 3 — Capture Payment

After approval, you will be automatically redirected to:

```
/api/payments/paypal/capture?token=ORDER_ID
```

Or test manually via Postman:

```http
GET http://localhost:8080/api/payments/paypal/capture?token=ORDER_ID
```

---

## 🔄 Payment Flows

See [`FLOW.md`](FLOW.md) for complete architecture diagrams, sequence flows (PayPal, Stripe Checkout, Stripe Intent), and webhook data flow.

---

## ⚠️ Important Notes

- **Capture is required** to complete the payment — without it, money is **NOT** transferred
- PayPal requires the following header even for empty-body requests (like capture):
  ```
  Content-Type: application/json
  ```
- Always **approve** the payment before calling capture — otherwise you'll receive: `ORDER_NOT_APPROVED`

---

## 🚀 Extending with a New Provider

To add a new payment provider:

**1. Create a strategy class implementing `PaymentStrategy`:**

```java
@Component("NEW_PROVIDER")
public class NewProviderStrategy implements PaymentStrategy {
    public Mono<PaymentCreateResponse> createPayment(PaymentRequest request) { ... }
    public Mono<PaymentCaptureResponse> capturePayment(String paymentId) { ... }
}
```

**2. The `@Component("NEW_PROVIDER")` annotation auto-registers it in the factory**

No changes needed in `PaymentStrategyFactory`, `PaymentService`, or `PaymentController`. ✅

```http
POST /api/payments/NEW_PROVIDER/create
POST /api/payments/NEW_PROVIDER/capture/{id}
```

---

## 🎯 Project Goals

- Demonstrate clean architecture using the **Strategy Pattern**
- Avoid SDK dependency — direct API integration only
- Make payment providers **easily pluggable**
- Provide a testable backend using Postman

---

## 🐳 Docker

### Building the Image

```bash
docker build -t payment-integration .
```

### Running with Docker

```bash
docker run -p 8080:8080 payment-integration
```

Then navigate to `http://localhost:8080` in your browser.

### Running with Docker Compose

Make sure you have a `docker-compose.yml` in your project root, then run:

```bash
docker-compose up
```

> 💡 Docker Compose is recommended for local development as it handles environment variables, port mapping, and service dependencies in one place.

---

## 🧩 Future Improvements

- [x] Add full **Stripe** integration (Checkout + Intent)
- [x] Add **Webhooks** for production reliability
- [x] Add **Database** for payment tracking & idempotency
- [ ] Add **Frontend** (React / Angular)

---

## 💡 Summary

This project shows how to:

- Build a **flexible payment system** with minimal coupling
- Integrate multiple payment providers using **different approaches**
- Use **design patterns** in real-world backend scenarios
- Implement **modern Java features** (records, Spring Boot 4.x)

---

## 👨‍💻 Author

**Mohamed Sayed**
<br>
**Kareem Khater**

---

> ⭐ If you found this project useful, consider giving it a star!
````
