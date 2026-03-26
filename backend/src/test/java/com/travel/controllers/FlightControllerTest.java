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
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class FlightControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private FlightRepository flightRepository;

    @Autowired
    private FlightBookingRepository flightBookingRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private String jwtToken;
    private UUID flightId;
    private static final String FLIGHT_CODE = "FL-loadtest-001";

    @BeforeEach
    void setUp() throws Exception {
        flightBookingRepository.deleteAll();
        flightRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();

        roleRepository.save(Role.builder().name("ROLE_TURISTA").build());

        // 1. Registrar y loguear usuario
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setFullName("Turista Test");
        signupRequest.setDocument("12345678");
        signupRequest.setEmail("turista@test.com");
        signupRequest.setPassword("password123");
        signupRequest.setRole(new HashSet<>(Collections.singletonList("ROLE_TURISTA")));

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isOk());

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("turista@test.com");
        loginRequest.setPassword("password123");

        MvcResult result = mockMvc.perform(post("/api/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        JwtResponse jwtResponse = objectMapper.readValue(response, JwtResponse.class);
        this.jwtToken = jwtResponse.getToken();

        // 2. Crear un vuelo inicial
        Flight flight = Flight.builder()
                .flightCode(FLIGHT_CODE)
                .origin("BOG")
                .destination("MDE")
                .departureDate(LocalDateTime.now().plusDays(1))
                .availableSeats(10)
                .basePrice(new java.math.BigDecimal("100.0"))
                .build();
        Flight savedFlight = flightRepository.save(flight);
        this.flightId = savedFlight.getId();
    }

    @Test
    void testCreateBookingSuccess() throws Exception {
        // Escenario: CP-TUR-016 - Programación exitosa con código único
        
        BookingRequest request = new BookingRequest();
        request.setFlightId(flightId);
        request.setDepartureDate(LocalDateTime.now().plusDays(1));
        request.setReturnDate(LocalDateTime.now().plusDays(5));

        mockMvc.perform(post("/api/flight-bookings")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookingCode").value(startsWith("TC-")))
                .andExpect(jsonPath("$.status").value("Confirmada"));

        // Verificación en historial (Postcondiciones)
        assertEquals(1, flightBookingRepository.findAll().size());
    }

    @Test
    void testBookingDecrementsSeats() throws Exception {
        // Escenario: CP-TUR-017 - Verificación de descuento de cupo en BD
        
        BookingRequest request = new BookingRequest();
        request.setFlightId(flightId);
        request.setDepartureDate(LocalDateTime.now().plusDays(1));
        request.setReturnDate(LocalDateTime.now().plusDays(5));

        // Reserva exitosa
        mockMvc.perform(post("/api/flight-bookings")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Verificación de cupos (Criterios de Aceptación)
        Flight updatedFlight = flightRepository.findById(flightId).orElseThrow();
        assertEquals(9, updatedFlight.getAvailableSeats()); // Era 10, ahora 9
    }

    @Test
    void testBookingInvalidDates() throws Exception {
        // Escenario: CP-TUR-018 / CP-TUR-019 - Validación de coherencia en fechas y alerta
        
        BookingRequest request = new BookingRequest();
        request.setFlightId(flightId);
        request.setDepartureDate(LocalDateTime.now().plusDays(5)); // Salida el día 5
        request.setReturnDate(LocalDateTime.now().plusDays(2));    // Regreso el día 2 (INVÁLIDO)

        mockMvc.perform(post("/api/flight-bookings")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value(containsString("La fecha de regreso no puede ser anterior")));
    }

    @Test
    void testNoDBPollutionOnFailedBooking() throws Exception {
        // Escenario: CP-TUR-020 - Verificación de no registro en BD tras intento fallido
        
        BookingRequest request = new BookingRequest();
        request.setFlightId(flightId);
        request.setDepartureDate(LocalDateTime.now().plusDays(5));
        request.setReturnDate(LocalDateTime.now().plusDays(2)); // Inválido

        mockMvc.perform(post("/api/flight-bookings")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        // Verificación: No hay reserva creada
        assertTrue(flightBookingRepository.findAll().isEmpty());
        // Verificación: Cupos del vuelo siguen intactos
        Flight flight = flightRepository.findById(flightId).orElseThrow();
        assertEquals(10, flight.getAvailableSeats());
    }
}
