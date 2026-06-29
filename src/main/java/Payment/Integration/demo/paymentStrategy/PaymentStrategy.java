package Payment.Integration.demo.paymentStrategy;

import Payment.Integration.demo.dto.PaymentCaptureResponse;
import Payment.Integration.demo.dto.PaymentCreateResponse;
import Payment.Integration.demo.dto.PaymentRequest;
import reactor.core.publisher.Mono;

public interface PaymentStrategy {
    Mono<PaymentCreateResponse> createPayment(PaymentRequest request);
    Mono<PaymentCaptureResponse> capturePayment(String paymentId);
}
