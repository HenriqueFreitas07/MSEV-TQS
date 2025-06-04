package tqs.msev.backend.controller;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import tqs.msev.backend.configuration.TestSecurityConfig;
import tqs.msev.backend.exception.GlobalExceptionHandler;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import app.getxray.xray.junit.customjunitxml.annotations.Requirement;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.hamcrest.Matchers.containsString;
import tqs.msev.backend.service.JwtService;
import tqs.msev.backend.service.PaymentService;
import tqs.msev.backend.entity.PaymentRequest;
import java.util.Map;

@WebMvcTest(PaymentController.class)
@Import({GlobalExceptionHandler.class, TestSecurityConfig.class})
class PaymentControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PaymentService paymentService;

    @MockitoBean
    private JwtService jwtService;

   @Test
   @WithUserDetails("test")
   @Requirement("MSEV-14")
   void whenPaymentRequestIsValid_thenReturnPaymentResponse() throws Exception {
       PaymentRequest paymentRequest = new PaymentRequest();
       paymentRequest.setAmount(1000L);
       paymentRequest.setCurrency("USD");
       paymentRequest.setPaymentMethod("pm_card_visa");

       Map<String, Object> mockResponse = Map.of(
           "status", "succeeded",
           "paymentIntentId", "pi_123456789",
           "clientSecret", "secret_123456789"
       );

       when(paymentService.createPayment(Mockito.any(PaymentRequest.class))).thenReturn(mockResponse);

       mockMvc.perform(post("/api/v1/payments")
               .with(csrf())
               .contentType("application/json")
               .content("{\"amount\": 1000, \"currency\": \"USD\", \"paymentMethod\": \"pm_card_visa\"}"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.status").value("succeeded"))
           .andExpect(jsonPath("$.paymentIntentId").value("pi_123456789"))
           .andExpect(jsonPath("$.clientSecret").value("secret_123456789"));
    }


    @Test
    @WithUserDetails("test")
    @Requirement("MSEV-14")
    void whenPaymentRequestFails_thenReturnErrorResponse() throws Exception {
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setAmount(1000L);
        paymentRequest.setCurrency("USD");
        paymentRequest.setPaymentMethod("pm_card_visa");
        doThrow(new NoSuchElementException("Payment method not found"))
            .when(paymentService).createPayment(Mockito.any(PaymentRequest.class));
        mockMvc.perform(post("/api/v1/payments")
                .with(csrf())
                .contentType("application/json")
                .content("{\"amount\": 1000, \"currency\": \"USD\", \"paymentMethod\": \"pm_card_visa\"}"))
            .andExpect(status().isNotFound())
            .andExpect(content().string(containsString("Payment method not found")));
    
    }

}