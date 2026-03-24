package com.travel.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.travel.dto.auth.JwtResponse;
import com.travel.dto.auth.LoginRequest;
import com.travel.dto.auth.SignupRequest;
import com.travel.model.auth.Role;
import com.travel.model.booking.Booking;
import com.travel.model.finance.Payment;
import com.travel.model.finance.PaymentMethod;
import com.travel.repository.RoleRepository;
import com.travel.repository.UserRepository;
import com.travel.repository.BookingRepository;
import com.travel.repository.finance.PaymentMethodRepository;
import com.travel.repository.finance.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashSet;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class PaymentTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PaymentMethodRepository paymentMethodRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private String jwtToken;
    private PaymentMethod paymentMethod;
    private Booking booking;

    @BeforeEach
    void setUp() throws Exception {
        paymentRepository.deleteAll();
        paymentMethodRepository.deleteAll();
        bookingRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();

        roleRepository.save(Role.builder().name("ROLE_TURISTA").build());

        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setFullName("Payer Test");
        signupRequest.setDocument("99887766");
        signupRequest.setEmail("payer@test.com");
        signupRequest.setPassword("password123");
        signupRequest.setRole(new HashSet<>(Collections.singletonList("ROLE_TURISTA")));

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isOk());

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("payer@test.com");
        loginRequest.setPassword("password123");

        MvcResult result = mockMvc.perform(post("/api/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        JwtResponse jwtResponse = objectMapper.readValue(result.getResponse().getContentAsString(), JwtResponse.class);
        this.jwtToken = jwtResponse.getToken();

        this.paymentMethod = paymentMethodRepository.save(PaymentMethod.builder()
                .user(userRepository.findByEmail("payer@test.com").orElseThrow())
                .cardNumber("1234 5678 9012 3456")
                .methodType("VISA")
                .build());

        this.booking = bookingRepository.save(Booking.builder()
                .user(userRepository.findByEmail("payer@test.com").orElseThrow())
                .totalAmount(new BigDecimal("100.00"))
                .status("Pendiente")
                .build());
    }

    @Test
    void testProcessPaymentMasking() throws Exception {
        // CP-TUR-030: Verificación de enmascaramiento en el flujo de pago
        Payment payment = Payment.builder()
                .booking(booking)
                .paymentMethod(paymentMethod)
                .amountPaid(new BigDecimal("100.00"))
                .paymentStatus("Aprobado")
                .build();

        mockMvc.perform(post("/api/payments")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payment)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.maskedCardNumber").value("**** **** **** 3456"))
                .andExpect(jsonPath("$.cardNumber").doesNotExist());
    }
}
