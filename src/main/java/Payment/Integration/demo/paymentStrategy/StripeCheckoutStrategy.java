package Payment.Integration.demo.paymentStrategy;

import Payment.Integration.demo.dto.PaymentCaptureResponse;
import Payment.Integration.demo.dto.PaymentCreateResponse;
import Payment.Integration.demo.dto.PaymentRequest;
import com.stripe.Stripe;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import com.stripe.exception.StripeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

import static reactor.core.publisher.Mono.just;

@Slf4j
@Component("STRIPE_CHECKOUT")
public class StripeCheckoutStrategy implements PaymentStrategy {

    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
    }

    @Value("${stripe.success.url:http://localhost:8080/api/payments/stripe/success}")
    private String successUrl;

    @Value("${stripe.cancel.url:http://localhost:8080/api/payments/stripe/cancel}")
    private String cancelUrl;

    @Override
    public Mono<PaymentCreateResponse> createPayment(PaymentRequest request) {
        try {
            long amountInCents = convertToCents(request.getAmount());

            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl(successUrl + "?session_id={CHECKOUT_SESSION_ID}")
                    .setCancelUrl(cancelUrl)
                    .addLineItem(
                            SessionCreateParams.LineItem.builder()
                                    .setQuantity(1L)
                                    .setPriceData(
                                            SessionCreateParams.LineItem.PriceData.builder()
                                                    .setCurrency(request.getCurrency().toLowerCase())
                                                    .setUnitAmount(amountInCents)
                                                    .setProductData(
                                                            SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                    .setName(request.getDescription())
                                                                    .build()
                                                    )
                                                    .build()
                                    )
                                    .build()
                    )
                    .build();

            Session session = Session.create(params);

            log.info("Created Stripe Checkout Session: {}", session.getId());

            return just(PaymentCreateResponse.builder()
                    .paymentId(session.getId())
                    .approvalUrl(session.getUrl())
                    .build());

        } catch (StripeException e) {
            log.error("Stripe checkout creation failed: {}", e.getMessage());
            throw new RuntimeException("Failed to create Stripe checkout: " + e.getMessage(), e);
        }
    }

    @Override
    public Mono<PaymentCaptureResponse> capturePayment(String sessionId) {
        try {
            Session session = Session.retrieve(sessionId);

            if ("complete".equals(session.getStatus())) {
                log.info("Stripe Checkout Session completed: {}", session.getId());
                return just(PaymentCaptureResponse.builder()
                        .paymentId(session.getPaymentIntent())
                        .status("succeeded")
                        .build());
            } else {
                throw new RuntimeException("Checkout session not completed. Status: " + session.getStatus());
            }

        } catch (StripeException e) {
            log.error("Stripe session retrieval failed for {}: {}", sessionId, e.getMessage());
            throw new RuntimeException("Failed to retrieve Stripe session: " + e.getMessage(), e);
        }
    }

    private long convertToCents(String amount) {
        try {
            BigDecimal decimal = new BigDecimal(amount);
            return decimal.multiply(new BigDecimal(100)).longValue();
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid amount format: " + amount, e);
        }
    }
}
