package Payment.Integration.demo.service;

import Payment.Integration.demo.dto.PaymentCaptureResponse;
import Payment.Integration.demo.dto.PaymentCreateResponse;
import Payment.Integration.demo.dto.PaymentRequest;
import Payment.Integration.demo.dto.PaymentTransactionResponse;
import Payment.Integration.demo.entity.PaymentTransaction;
import Payment.Integration.demo.paymentStrategy.PaymentStrategyFactory;
import Payment.Integration.demo.repository.PaymentTransactionRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PaymentService {

    private final PaymentStrategyFactory factory;
    private final PaymentTransactionRepository transactionRepository;

    public PaymentService(PaymentStrategyFactory factory, PaymentTransactionRepository transactionRepository) {
        this.factory = factory;
        this.transactionRepository = transactionRepository;
    }

    public Mono<PaymentCreateResponse> create(String type, PaymentRequest request) {
        return factory.getStrategy(type).createPayment(request);
    }

    public Mono<PaymentCaptureResponse> capture(String type, String paymentId) {
        return factory.getStrategy(type).capturePayment(paymentId);
    }

    public PaymentTransactionResponse getTransaction(String paymentId) {
        PaymentTransaction txn = transactionRepository.findByPaymentId(paymentId)
                .orElseThrow(() -> new RuntimeException("Transaction not found: " + paymentId));
        return PaymentTransactionResponse.fromEntity(txn);
    }

    public List<PaymentTransactionResponse> getAllTransactions() {
        return transactionRepository.findAll().stream()
                .map(PaymentTransactionResponse::fromEntity)
                .collect(Collectors.toList());
    }
}