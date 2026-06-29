package Payment.Integration.demo;

import Payment.Integration.demo.dto.PaymentRequest;
import Payment.Integration.demo.paymentStrategy.StripeCheckoutStrategy;
import Payment.Integration.demo.paymentStrategy.StripeIntentStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class StripeStrategyTest {

    @Nested
    class StripeCheckoutStrategyTest {
        private StripeCheckoutStrategy strategy;
        private PaymentRequest request;

        @BeforeEach
        void setUp() {
            strategy = new StripeCheckoutStrategy();
            ReflectionTestUtils.setField(strategy, "stripeSecretKey", "sk_test_dummy");
            ReflectionTestUtils.setField(strategy, "successUrl", "http://localhost:8080/success");
            ReflectionTestUtils.setField(strategy, "cancelUrl", "http://localhost:8080/cancel");

            request = new PaymentRequest();
            request.setAmount("10.00");
            request.setCurrency("USD");
            request.setDescription("Test payment");
        }

        @Test
        void createPayment_InvalidAmount_ThrowsException() {
            request.setAmount("invalid");
            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> strategy.createPayment(request));
            assertTrue(ex.getMessage().contains("Invalid amount format"));
        }

        @Test
        void convertToCents_ValidAmount_ReturnsCorrectValue() {
            Long result = (Long) ReflectionTestUtils.invokeMethod(
                    strategy, "convertToCents", "10.00");
            assertEquals(1000L, result);
        }

        @Test
        void convertToCents_FractionalAmount_ReturnsCorrectValue() {
            Long result = (Long) ReflectionTestUtils.invokeMethod(
                    strategy, "convertToCents", "10.99");
            assertEquals(1099L, result);
        }

        @Test
        void convertToCents_ZeroAmount_ReturnsZero() {
            Long result = (Long) ReflectionTestUtils.invokeMethod(
                    strategy, "convertToCents", "0.00");
            assertEquals(0L, result);
        }

        @Test
        void convertToCents_InvalidAmount_ThrowsException() {
            assertThrows(IllegalArgumentException.class,
                    () -> ReflectionTestUtils.invokeMethod(
                            strategy, "convertToCents", "invalid"));
        }
    }

    @Nested
    class StripeIntentStrategyTest {
        private StripeIntentStrategy strategy;
        private PaymentRequest request;

        @BeforeEach
        void setUp() {
            strategy = new StripeIntentStrategy();
            ReflectionTestUtils.setField(strategy, "stripeSecretKey", "sk_test_dummy");

            request = new PaymentRequest();
            request.setAmount("10.00");
            request.setCurrency("USD");
            request.setDescription("Test payment");
        }

        @Test
        void createPayment_InvalidAmount_ThrowsException() {
            request.setAmount("invalid");
            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> strategy.createPayment(request));
            assertTrue(ex.getMessage().contains("Invalid amount format"));
        }

        @Test
        void convertToCents_ValidAmount_ReturnsCorrectValue() {
            Long result = (Long) ReflectionTestUtils.invokeMethod(
                    strategy, "convertToCents", "10.00");
            assertEquals(1000L, result);
        }

        @Test
        void convertToCents_FractionalAmount_ReturnsCorrectValue() {
            Long result = (Long) ReflectionTestUtils.invokeMethod(
                    strategy, "convertToCents", "10.99");
            assertEquals(1099L, result);
        }

        @Test
        void convertToCents_ZeroAmount_ReturnsZero() {
            Long result = (Long) ReflectionTestUtils.invokeMethod(
                    strategy, "convertToCents", "0.00");
            assertEquals(0L, result);
        }

        @Test
        void convertToCents_InvalidAmount_ThrowsException() {
            assertThrows(IllegalArgumentException.class,
                    () -> ReflectionTestUtils.invokeMethod(
                            strategy, "convertToCents", "invalid"));
        }
    }
}
