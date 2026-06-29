package Payment.Integration.demo.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentCaptureResponse {
    private String paymentId;
    private String status;
}
