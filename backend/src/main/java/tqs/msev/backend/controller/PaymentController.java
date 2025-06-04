package tqs.msev.backend.controller;


import com.stripe.Stripe;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tqs.msev.backend.entity.PaymentRequest;
import tqs.msev.backend.service.PaymentService;

import java.util.Map;

import jakarta.validation.Valid;


@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(@Value("${stripe.api.key}") String stripeApiKey, PaymentService paymentService) {
        Stripe.apiKey = stripeApiKey;
        System.out.println("Stripe API Key set to: " + stripeApiKey);
        this.paymentService = paymentService;
    }
    
    @PostMapping
    public Map<String, Object> createPayment(@Valid PaymentRequest paymentRequest) {
      return paymentService.createPayment(paymentRequest);
    }
}
