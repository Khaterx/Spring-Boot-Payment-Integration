package Payment.Integration.demo.dto;

import Payment.Integration.demo.entity.PaymentTransaction;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PaymentTransactionResponse {
    private Long id;
    private String paymentId;
    private String provider;
    private String amount;
    private String currency;
    private String status;
    private String customerEmail;
    private LocalDateTime createdAt;

    public static PaymentTransactionResponse fromEntity(PaymentTransaction txn) {
        return PaymentTransactionResponse.builder()
                .id(txn.getId())
                .paymentId(txn.getPaymentId())
                .provider(txn.getProvider())
                .amount(txn.getAmount())
                .currency(txn.getCurrency())
                .status(txn.getStatus())
                .customerEmail(txn.getCustomerEmail())
                .createdAt(txn.getCreatedAt())
                .build();
    }
}
