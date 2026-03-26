package com.travel.controllers; 

import com.fasterxml.jackson.databind.ObjectMapper;
import com.travel.dto.auth.JwtResponse;
import com.travel.dto.auth.LoginRequest;
import com.travel.dto.auth.SignupRequest;
import com.travel.model.Partner;
import com.travel.model.auth.Role;
import com.travel.repository.FlightBookingRepository;
import com.travel.repository.FlightRepository;
import com.travel.repository.PartnerRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class PartnerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PartnerRepository partnerRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FlightBookingRepository flightBookingRepository;

    @Autowired
    private FlightRepository flightRepository;

    private String adminToken;

    @BeforeEach
    void setUp() throws Exception {
        flightBookingRepository.deleteAll();
        flightRepository.deleteAll();
        partnerRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();

        roleRepository.save(Role.builder().name("ROLE_ADMIN").build());

        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setFullName("Admin User");
        signupRequest.setDocument("11112222");
        signupRequest.setEmail("admin@test.com");
        signupRequest.setPassword("admin123");
        signupRequest.setRole(new HashSet<>(Collections.singletonList("ROLE_ADMIN")));

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isOk());

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("admin@test.com");
        loginRequest.setPassword("admin123");

        MvcResult result = mockMvc.perform(post("/api/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        JwtResponse jwtResponse = objectMapper.readValue(result.getResponse().getContentAsString(), JwtResponse.class);
        this.adminToken = jwtResponse.getToken();
    }

    @Test
    void testPartnerLifecycle() throws Exception {
        // CP-ADM-034: Creación satisfactoria
        Partner partner = Partner.builder()
                .partnerId("SOC-123")
                .companyName("Turismo Global")
                .address("Calle Principal 456")
                .phone("555-0101")
                .status("Activo")
                .build();

        mockMvc.perform(post("/api/partners")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(partner)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.companyName").value("Turismo Global"));

        // CP-ADM-037: Restricción por ID duplicado
        mockMvc.perform(post("/api/partners")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(partner)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Socio ya existente")));

        // CP-ADM-036: Localización por Búsqueda
        mockMvc.perform(get("/api/partners/search")
                .param("query", "Global")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].partnerId").value("SOC-123"));

        // CP-ADM-039: Modificación de datos
        partner.setPhone("555-9999");
        mockMvc.perform(put("/api/partners/SOC-123")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(partner)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.phone").value("555-9999"));

        // CP-ADM-041: Bloqueo por campo vacío
        partner.setPhone("");
        mockMvc.perform(put("/api/partners/SOC-123")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(partner)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Todos los campos son obligatorios")));
    }
}
