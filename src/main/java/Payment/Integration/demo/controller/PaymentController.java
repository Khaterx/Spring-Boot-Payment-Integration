package Payment.Integration.demo.controller;

import Payment.Integration.demo.dto.PaymentCaptureResponse;
import Payment.Integration.demo.dto.PaymentCreateResponse;
import Payment.Integration.demo.dto.PaymentRequest;
import Payment.Integration.demo.dto.PaymentTransactionResponse;
import Payment.Integration.demo.service.PaymentService;
import com.stripe.Stripe;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService service;

    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    public PaymentController(PaymentService service) {
        this.service = service;
    }

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
    }

    @GetMapping
    public List<PaymentTransactionResponse> getAllPayments() {
        return service.getAllTransactions();
    }

    @GetMapping("/stripe/success")
    public Mono<String> paymentSuccess(@RequestParam("session_id") String sessionId) {
        return Mono.just("Payment submitted! Session: " + sessionId + "\nCheck status at: GET /api/payments/" + sessionId);
    }

    @GetMapping("/{paymentId}")
    public PaymentTransactionResponse getPayment(@PathVariable String paymentId) {
        return service.getTransaction(paymentId);
    }

    @GetMapping("/stripe/cancel")
    public Mono<String> paymentCancel() {
        return Mono.just("Payment was cancelled.");
    }

    @PostMapping("/{type}/create")
    public Mono<PaymentCreateResponse> create(
            @PathVariable String type,
            @RequestBody PaymentRequest request) {
        return service.create(type, request);
    }

    @PostMapping("/{type}/capture/{id}")
    public Mono<PaymentCaptureResponse> capture(
            @PathVariable String type,
            @PathVariable String id) {
        return service.capture(type, id);
    }
}