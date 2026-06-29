package Payment.Integration.demo.paymentStrategy;

import Payment.Integration.demo.dto.PaymentCaptureResponse;
import Payment.Integration.demo.dto.PaymentCreateResponse;
import Payment.Integration.demo.dto.PaymentRequest;
import com.stripe.Stripe;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.exception.StripeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

import static reactor.core.publisher.Mono.just;

@Slf4j
@Component("STRIPE_INTENT")
public class StripeIntentStrategy implements PaymentStrategy {

    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
    }

    @Override
    public Mono<PaymentCreateResponse> createPayment(PaymentRequest request) {
        try {
            long amountInCents = convertToCents(request.getAmount());

            PaymentIntentCreateParams.AutomaticPaymentMethods automaticPaymentMethods =
                    PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                            .setEnabled(true)
                            .setAllowRedirects(PaymentIntentCreateParams.AutomaticPaymentMethods.AllowRedirects.NEVER)
                            .build();

            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amountInCents)
                    .setCurrency(request.getCurrency().toLowerCase())
                    .setDescription(request.getDescription())
                    .setCaptureMethod(PaymentIntentCreateParams.CaptureMethod.MANUAL)
                    .setAutomaticPaymentMethods(automaticPaymentMethods)
                    .build();

            PaymentIntent paymentIntent = PaymentIntent.create(params);

            log.info("Created Stripe PaymentIntent: {}", paymentIntent.getId());

            return just(PaymentCreateResponse.builder()
                    .paymentId(paymentIntent.getId())
                    .approvalUrl(paymentIntent.getClientSecret())
                    .build());

        } catch (StripeException e) {
            log.error("Stripe payment creation failed: {}", e.getMessage());
            throw new RuntimeException("Failed to create Stripe payment: " + e.getMessage(), e);
        }
    }

    @Override
    public Mono<PaymentCaptureResponse> capturePayment(String paymentIntentId) {
        try {
            PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);
            PaymentIntent capturedIntent = paymentIntent.capture();

            log.info("Captured Stripe PaymentIntent: {} with status: {}",
                    capturedIntent.getId(), capturedIntent.getStatus());

            return just(PaymentCaptureResponse.builder()
                    .paymentId(capturedIntent.getId())
                    .status(capturedIntent.getStatus())
                    .build());

        } catch (StripeException e) {
            log.error("Stripe payment capture failed for {}: {}", paymentIntentId, e.getMessage());
            throw new RuntimeException("Failed to capture Stripe payment: " + e.getMessage(), e);
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
