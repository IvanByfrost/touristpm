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

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class FlightBookingSearchTest {

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
    private final String TEST_EMAIL = "test.viajero@mail.com";
    private final String TEST_DOC = "10203040";
    private final String TEST_NAME = "Iván Darío Ruiz";

    @BeforeEach
    void setUp() throws Exception {
        flightBookingRepository.deleteAll();
        flightRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();

        roleRepository.save(Role.builder().name("ROLE_TURISTA").build());

        // 1. Crear usuario con datos específicos (CP-TUR-021/022/023)
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setFullName(TEST_NAME);
        signupRequest.setDocument(TEST_DOC);
        signupRequest.setEmail(TEST_EMAIL);
        signupRequest.setPassword("password123");
        signupRequest.setRole(new HashSet<>(Collections.singletonList("ROLE_TURISTA")));

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isOk());

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(TEST_EMAIL);
        loginRequest.setPassword("password123");

        MvcResult loginResult = mockMvc.perform(post("/api/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andReturn();
        
        JwtResponse jwtResponse = objectMapper.readValue(loginResult.getResponse().getContentAsString(), JwtResponse.class);
        this.jwtToken = jwtResponse.getToken();

        // 2. Crear vuelo y reserva
        Flight flight = flightRepository.save(Flight.builder()
                .flightCode("FL-001")
                .origin("BOG")
                .destination("MDE")
                .departureDate(LocalDateTime.now().plusDays(1))
                .availableSeats(10)
                .build());

        BookingRequest bookingReq = new BookingRequest();
        bookingReq.setFlightId(flight.getId());
        bookingReq.setDepartureDate(LocalDateTime.now().plusDays(1));
        bookingReq.setReturnDate(LocalDateTime.now().plusDays(5));

        mockMvc.perform(post("/api/flight-bookings")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bookingReq)))
                .andExpect(status().isOk());
    }

    @Test
    void testSearchByEmail() throws Exception {
        // Escenario: CP-TUR-021 - Consulta exitosa mediante correo electrónico
        mockMvc.perform(get("/api/flight-bookings/search")
                .param("email", TEST_EMAIL)
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].bookingCode", startsWith("TC-")));
    }

    @Test
    void testSearchByDocument() throws Exception {
        // Escenario: CP-TUR-022 - Consulta mediante documento de identidad y rendimiento
        long start = System.currentTimeMillis();
        
        mockMvc.perform(get("/api/flight-bookings/search")
                .param("document", TEST_DOC)
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].bookingCode", startsWith("TC-")));
        
        long duration = System.currentTimeMillis() - start;
        assertTrue(duration < 2000, "El tiempo de respuesta debe ser óptimo (< 2s)");
    }

    @Test
    void testSearchByName() throws Exception {
        // Escenario: CP-TUR-023 - Consulta mediante nombre del titular
        mockMvc.perform(get("/api/flight-bookings/search")
                .param("name", "Iván Darío")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].bookingCode", startsWith("TC-")));
    }

    @Test
    void testSearchNoResults() throws Exception {
        // Escenario: CP-TUR-024/025 - Notificación de "No resultados" y privacidad
        mockMvc.perform(get("/api/flight-bookings/search")
                .param("email", "inexistente@test.com")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").value("No se encontraron reservas con los datos ingresados"));
    }
}
