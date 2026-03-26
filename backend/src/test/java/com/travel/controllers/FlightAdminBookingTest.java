package com.travel.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.travel.dto.BookingRequest;
import com.travel.dto.auth.JwtResponse;
import com.travel.dto.auth.LoginRequest;
import com.travel.dto.auth.SignupRequest;
import com.travel.model.Flight;
import com.travel.model.auth.Role;
import com.travel.repository.FlightBookingRepository;
import com.travel.repository.FlightRepository;
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

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class FlightAdminBookingTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FlightRepository flightRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private FlightBookingRepository flightBookingRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminToken;
    private java.util.UUID flightId;

    @BeforeEach
    void setUp() throws Exception {
        flightBookingRepository.deleteAll();
        flightRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();

        roleRepository.save(Role.builder().name("ROLE_ADMIN").build());

        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setFullName("Admin Root");
        signupRequest.setDocument("00000000");
        signupRequest.setEmail("admin@travel.com");
        signupRequest.setPassword("admin123");
        signupRequest.setRole(new HashSet<>(Collections.singletonList("ROLE_ADMIN")));

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isOk());

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("admin@travel.com");
        loginRequest.setPassword("admin123");

        MvcResult result = mockMvc.perform(post("/api/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        JwtResponse jwtResponse = objectMapper.readValue(result.getResponse().getContentAsString(), JwtResponse.class);
        this.adminToken = jwtResponse.getToken();

        Flight flight = flightRepository.save(Flight.builder()
                .flightCode("ADM-001")
                .origin("BOG")
                .destination("MDE")
                .availableSeats(10)
                .basePrice(new java.math.BigDecimal("100.0"))
                .departureDate(LocalDateTime.now().plusDays(1))
                .build());
        this.flightId = flight.getId();
    }

    @Test
    void testAdminReservationTraceability() throws Exception {
        // CP-ADM-043: Generación de reserva administrativa con marca de trazabilidad
        BookingRequest request = new BookingRequest();
        request.setFlightId(flightId);
        request.setDepartureDate(LocalDateTime.now().plusDays(1));
        request.setReturnDate(LocalDateTime.now().plusDays(5));

        mockMvc.perform(post("/api/flight-bookings/admin")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isAdministrative").value(true));
    }
}
