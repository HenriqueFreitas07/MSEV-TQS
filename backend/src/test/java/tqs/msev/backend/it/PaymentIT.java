package tqs.msev.backend.it;


import app.getxray.xray.junit.customjunitxml.annotations.Requirement;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;

import tqs.msev.backend.configuration.TestDatabaseConfig;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import tqs.msev.backend.repository.UserRepository;
import tqs.msev.backend.entity.User;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Import(TestDatabaseConfig.class)
class PaymentIT {


    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @BeforeAll
    void beforeAll() {
        User test = User.builder()
                .email("test")
                .name("test")
                .password("test")
                .isOperator(true)
                .build();
        userRepository.saveAndFlush(test);
    }

    @AfterAll
    void afterAll() {
        userRepository.deleteAll();
    }
    
    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.jpa.hibernate.ddl-auto", ()->"create-drop");
        registry.add("security.jwt.secret-key", () -> "f9924db12318f6a0f1bcfa6e5d0342b65a51022a48a8246cdaa3b1a45493b6b4");
        registry.add("security.jwt.expiration-time", () -> "360000");
    }


    @Test
    @WithUserDetails("test")
    @Requirement("MSEV-14")
    void whenPaymentRequestIsValid_thenReturnPaymentResponse() throws Exception {
        
        String paymentRequestJson = "{ \"amount\": 1000, \"currency\": \"USD\", \"paymentMethod\": \"pm_card_visa\" }";

        mockMvc.perform(post("/api/v1/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(paymentRequestJson)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("succeeded"));
    }

    @Test
    @WithUserDetails("test")
    @Requirement("MSEV-14")
    void whenPaymentRequestIsInvalid_thenReturnErrorResponse() throws Exception {
        
        String paymentRequestJson = "{ \"amount\": 1000, \"currency\": \"USD\", \"paymentMethod\": \"pm_card_chargeCustomerFail\" }";

        mockMvc.perform(post("/api/v1/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(paymentRequestJson)
                .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"));
    }

}
