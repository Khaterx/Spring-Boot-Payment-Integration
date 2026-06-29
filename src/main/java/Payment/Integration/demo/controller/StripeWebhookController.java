package Payment.Integration.demo.controller;

import Payment.Integration.demo.entity.PaymentTransaction;
import Payment.Integration.demo.repository.PaymentTransactionRepository;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.checkout.Session;
import com.stripe.net.ApiResource;
import com.stripe.net.Webhook;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/webhooks")
public class StripeWebhookController {

    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    private final PaymentTransactionRepository repository;

    public StripeWebhookController(PaymentTransactionRepository repository) {
        this.repository = repository;
    }

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
    }

    @PostMapping("/stripe")
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        try {
            Event event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
            log.info("Stripe webhook received: {}", event.getType());

            switch (event.getType()) {
                case "checkout.session.completed" -> {
                    Session session = deserializeSession(event);

                    PaymentTransaction transaction = new PaymentTransaction();
                    transaction.setPaymentId(session.getId());
                    transaction.setProvider("STRIPE_CHECKOUT");
                    transaction.setAmount(String.valueOf(session.getAmountTotal() / 100.0));
                    transaction.setCurrency(session.getCurrency());
                    transaction.setStatus(session.getStatus());
                    transaction.setCustomerEmail(session.getCustomerDetails().getEmail());
                    transaction.setRawWebhookPayload(payload);

                    repository.save(transaction);
                    log.info("Saved payment transaction: {} ({})", session.getId(), session.getStatus());
                }
                case "checkout.session.expired" -> {
                    Session session = deserializeSessionQuietly(event);
                    if (session != null) {
                        PaymentTransaction txn = repository.findByPaymentId(session.getId())
                                .orElseGet(() -> {
                                    PaymentTransaction t = new PaymentTransaction();
                                    t.setPaymentId(session.getId());
                                    t.setProvider("STRIPE_CHECKOUT");
                                    t.setAmount(String.valueOf(session.getAmountTotal() / 100.0));
                                    t.setCurrency(session.getCurrency());
                                    t.setRawWebhookPayload(payload);
                                    return t;
                                });
                        txn.setStatus("expired");
                        repository.save(txn);
                        log.info("Updated transaction {} to expired", session.getId());
                    }
                }
                default -> log.info("Unhandled event type: {}", event.getType());
            }

            return ResponseEntity.ok("Received");
        } catch (SignatureVerificationException e) {
            log.warn("Webhook signature verification failed: {}", e.getMessage());
            return ResponseEntity.status(400).body("Invalid signature");
        }
    }

    private Session deserializeSession(Event event) {
        EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
        if (deserializer.getObject().isPresent()) {
            return (Session) deserializer.getObject().get();
        }
        try {
            return ApiResource.GSON.fromJson(deserializer.getRawJson(), Session.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize session", e);
        }
    }

    private Session deserializeSessionQuietly(Event event) {
        EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
        if (deserializer.getObject().isPresent()) {
            return (Session) deserializer.getObject().get();
        }
        try {
            return ApiResource.GSON.fromJson(deserializer.getRawJson(), Session.class);
        } catch (Exception e) {
            log.warn("Failed to deserialize session for expired event", e);
            return null;
        }
    }
}
