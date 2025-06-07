package tqs.msev.backend.service;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithUserDetails;

import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;

import org.mockito.MockedStatic;

import tqs.msev.backend.entity.PaymentRequest;
import app.getxray.xray.junit.customjunitxml.annotations.Requirement;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @InjectMocks
    private  PaymentService paymentService;

    @Test
    @WithUserDetails("test")
    @Requirement("MSEV-14")
    void whenPaymentRequestIsValid_thenReturnPaymentResponse(){

        PaymentIntent mockIntent = new PaymentIntent();
        mockIntent.setId("pi_test_123");
        mockIntent.setStatus("succeeded");

        try (MockedStatic<PaymentIntent> mocked = Mockito.mockStatic(PaymentIntent.class)) {
            mocked.when(() -> PaymentIntent.create(Mockito.any(PaymentIntentCreateParams.class)))
                .thenReturn(mockIntent);

            PaymentRequest paymentRequest = new PaymentRequest();
            paymentRequest.setAmount(1000L);
            paymentRequest.setCurrency("USD");
            paymentRequest.setPaymentMethod("pm_card_visa");

            Map<String, Object> response = paymentService.createPayment(paymentRequest);

            assertNotNull(response);
            assertEquals("succeeded", response.get("status"));
        }
    }

    @Test
    @WithUserDetails("test")
    @Requirement("MSEV-14")
    void whenPaymentRequestIsInvalid_thenReturnErrorResponse() {
       PaymentIntent mockIntent = new PaymentIntent();
        mockIntent.setId("pi_test_123");
        mockIntent.setStatus("error");

        try (MockedStatic<PaymentIntent> mocked = Mockito.mockStatic(PaymentIntent.class)) {
            mocked.when(() -> PaymentIntent.create(Mockito.any(PaymentIntentCreateParams.class)))
                .thenReturn(mockIntent);

            PaymentRequest paymentRequest = new PaymentRequest();
            paymentRequest.setAmount(1000L);
            paymentRequest.setCurrency("USD");
            paymentRequest.setPaymentMethod("pm_card_visa");

            Map<String, Object> response = paymentService.createPayment(paymentRequest);

            assertNotNull(response);
            assertEquals("error", response.get("status"));
        }
    }
    
}
