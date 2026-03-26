package com.travel.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.travel.dto.auth.JwtResponse;
import com.travel.dto.auth.LoginRequest;
import com.travel.dto.auth.SignupRequest;
import com.travel.model.finance.PaymentMethod;
import com.travel.model.auth.Role;
import com.travel.repository.FlightBookingRepository;
import com.travel.repository.FlightRepository;
import com.travel.repository.finance.PaymentMethodRepository;
import com.travel.repository.RoleRepository;
import com.travel.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Collections;
import java.util.HashSet;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class PaymentMethodTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PaymentMethodRepository paymentMethodRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FlightBookingRepository flightBookingRepository;

    @Autowired
    private FlightRepository flightRepository;

    private String jwtToken;

    @BeforeEach
    void setUp() throws Exception {
        flightBookingRepository.deleteAll();
        flightRepository.deleteAll();
        paymentMethodRepository.deleteAll();
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
    }

    @Test
    void testAddAndMaskPaymentMethod() throws Exception {
        // Escenario: CP-TUR-030 - Vinculación de tarjeta y enmascaramiento de datos
        PaymentMethod pm = PaymentMethod.builder()
                .cardNumber("1234 5678 9876 5432")
                .holderName("PAYER TEST")
                .expirationDate("12/28")
                .methodType("VISA")
                .build();

        mockMvc.perform(post("/api/payment-methods")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(pm)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.maskedCardNumber").value("**** **** **** 5432"));
        
        // Verificar en listado
        mockMvc.perform(get("/api/payment-methods")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].maskedCardNumber").value("**** **** **** 5432"))
                .andExpect(jsonPath("$[0].cardNumber").doesNotExist());
    }

    @Test
    void testRejectIncompleteCardNumber() throws Exception {
        // CP-TUR-032: Bloqueo por número de tarjeta incompleto
        PaymentMethod pm = PaymentMethod.builder()
                .cardNumber("1234 5678") // Incompleta (< 16 dígitos)
                .holderName("PAYER TEST")
                .expirationDate("12/28")
                .methodType("VISA")
                .build();

        mockMvc.perform(post("/api/payment-methods")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(pm)))
                .andExpect(status().isBadRequest());
    }
}
