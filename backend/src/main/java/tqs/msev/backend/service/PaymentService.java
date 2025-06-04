package tqs.msev.backend.service;

import java.util.HashMap;
import java.util.Map;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import tqs.msev.backend.entity.PaymentRequest;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {
    
    public Map<String, Object> createPayment(PaymentRequest paymentRequest) {
        Map<String, Object> response = new HashMap<>();
        try{
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(paymentRequest.getAmount())
                .setCurrency(paymentRequest.getCurrency())
                .setPaymentMethod(paymentRequest.getPaymentMethod())
                .setConfirm(true)
                .build();
            
            PaymentIntent paymentIntent = PaymentIntent.create(params);

            response.put("status", paymentIntent.getStatus());
            response.put("paymentIntentId", paymentIntent.getId());
            response.put("clientSecret", paymentIntent.getClientSecret());
        } catch (Exception e) {
            response.put("error", e.getMessage());
        }
         return response; 
    }
}
