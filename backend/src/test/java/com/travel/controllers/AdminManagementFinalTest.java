package com.travel.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.travel.dto.BookingRequest;
import com.travel.dto.auth.JwtResponse;
import com.travel.dto.auth.LoginRequest;
import com.travel.dto.auth.SignupRequest;
import com.travel.model.Flight;
import com.travel.model.FlightBooking;
import com.travel.model.catalog.Rate;
import com.travel.model.auth.Role;
import com.travel.repository.*;
import com.travel.repository.catalog.RateRepository;
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
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AdminManagementFinalTest {

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
    private RateRepository rateRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminToken;
    private Flight flight;

    @BeforeEach
    void setUp() throws Exception {
        flightBookingRepository.deleteAll();
        flightRepository.deleteAll();
        rateRepository.deleteAll();
        auditLogRepository.deleteAll();
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
                .andReturn();

        JwtResponse jwtResponse = objectMapper.readValue(result.getResponse().getContentAsString(), JwtResponse.class);
        this.adminToken = jwtResponse.getToken();

        this.flight = flightRepository.save(Flight.builder()
                .flightCode("TEST-999")
                .origin("BOG")
                .destination("MDE")
                .availableSeats(10)
                .basePrice(new BigDecimal("100.00"))
                .departureDate(LocalDateTime.now().plusDays(1))
                .build());
    }

    @Test
    void testReservationCancellationFlow() throws Exception {
        // Create a booking first
        BookingRequest request = new BookingRequest();
        request.setFlightId(flight.getId());
        request.setDepartureDate(LocalDateTime.now().plusDays(1));
        request.setReturnDate(LocalDateTime.now().plusDays(5));

        MvcResult res = mockMvc.perform(post("/api/flight-bookings")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();
        
        String content = res.getResponse().getContentAsString();
        System.out.println("DEBUG - Booking Content: " + content);
        UUID bookingId = UUID.fromString(objectMapper.readTree(content).get("id").asText());

        // CP-ADM-052/053/054: Anulación y liberación de cupos
        String justification = "Cancelación por solicitud del cliente - Prueba";
        mockMvc.perform(post("/api/flight-bookings/" + bookingId + "/cancel")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"justification\": \"" + justification + "\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("Reserva anulada y cupos liberados"));

        // Verify status
        FlightBooking booking = flightBookingRepository.findById(bookingId).orElseThrow();
        assertThat(booking.getStatus()).isEqualTo("Cancelada");
        assertThat(booking.getCancellationJustification()).isEqualTo(justification);

        // Verify seats liberation (Original 10 -> -1 for booking = 9 -> +1 for cancel = 10)
        Flight updatedFlight = flightRepository.findById(flight.getId()).orElseThrow();
        assertThat(updatedFlight.getAvailableSeats()).isEqualTo(10);

        // CP-ADM-055/056: Mandatory justification
        mockMvc.perform(post("/api/flight-bookings/" + bookingId + "/cancel")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"justification\": \"   \"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testRateManagementAndHistoricalProtection() throws Exception {
        // CP-ADM-058/060: Rate update and decimals
        Rate rate = rateRepository.save(Rate.builder()
                .description("Cargo de Gestión")
                .amount(new BigDecimal("25.50"))
                .serviceType("ADMIN")
                .build());

        Rate details = new Rate();
        details.setDescription("Cargo Actualizado");
        details.setAmount(new BigDecimal("30.75"));
        details.setServiceType("ADMIN");

        mockMvc.perform(put("/api/rates/" + rate.getRateId())
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(details)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(30.75));

        // CP-ADM-061: No negative prices
        details.setAmount(new BigDecimal("-5.00"));
        mockMvc.perform(put("/api/rates/" + rate.getRateId())
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(details)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("El valor de la tarifa debe ser superior a cero"));

        // CP-ADM-062: No zero prices
        details.setAmount(BigDecimal.ZERO);
        mockMvc.perform(put("/api/rates/" + rate.getRateId())
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(details)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("El valor de la tarifa debe ser superior a cero"));

        // CP-ADM-059: Historical Price Protection
        // 1. Create booking with current flight price ($100)
        BookingRequest request = new BookingRequest();
        request.setFlightId(flight.getId());
        request.setDepartureDate(LocalDateTime.now().plusDays(1));
        request.setReturnDate(LocalDateTime.now().plusDays(5));

        MvcResult res = mockMvc.perform(post("/api/flight-bookings")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andReturn();
        
        String content = res.getResponse().getContentAsString();
        System.out.println("DEBUG - Protection Booking Content: " + content);
        UUID bookingId = UUID.fromString(objectMapper.readTree(content).get("id").asText());

        // 2. Change flight base price to $150
        flight.setBasePrice(new BigDecimal("150.00"));
        flightRepository.save(flight);

        // 3. Verify booking still has $100
        FlightBooking booking = flightBookingRepository.findById(bookingId).orElseThrow();
        assertThat(booking.getBookingPrice()).isEqualByComparingTo("100.00");
    }

    @Test
    void testHistoricalEditBlockNoAuditLog() throws Exception {
        // Create booking and set status to Ejecutada
        FlightBooking booking = flightBookingRepository.save(FlightBooking.builder()
                .bookingCode("H-001")
                .flight(flight)
                .user(userRepository.findByEmail("admin@travel.com").orElseThrow())
                .status("Ejecutada")
                .departureDate(LocalDateTime.now().minusDays(1))
                .returnDate(LocalDateTime.now().minusDays(5))
                .bookingPrice(new BigDecimal("50.00"))
                .build());

        // CP-ADM-051: Bloqueo de persistencia y ausencia de logs por intento fallido
        int logsBefore = auditLogRepository.findAll().size();

        mockMvc.perform(put("/api/flight-bookings/" + booking.getId())
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\": \"Finalizada\"}"))
                .andExpect(status().isBadRequest());

        int logsAfter = auditLogRepository.findAll().size();
        assertThat(logsAfter).isEqualTo(logsBefore); // No logs generated for failed attempt
    }
}
