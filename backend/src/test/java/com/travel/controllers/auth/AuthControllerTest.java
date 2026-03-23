package com.travel.controllers.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.travel.dto.auth.SignupRequest;
import com.travel.model.auth.Role;
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

import java.util.Collections;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        roleRepository.deleteAll();
        
        // Pre-condición: El rol debe existir en la BD
        roleRepository.save(Role.builder().name("ROLE_TURISTA").build());
        roleRepository.save(Role.builder().name("ROLE_USER").build());
    }

    @Test
    void testRegisterTouristSuccess() throws Exception {
        // Escenario: HU-TUR-001-A - Registro de TURISTA
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setFullName("Juan Perez");
        signupRequest.setEmail("juan.perez@example.com");
        signupRequest.setPassword("password123");
        signupRequest.setRole(new HashSet<>(Collections.singletonList("ROLE_TURISTA")));

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isOk());

        // Post-condiciones: El nuevo usuario tiene credenciales válidas en la BD
        assertTrue(userRepository.findByEmail("juan.perez@example.com").isPresent());
        
        // Notas: Verificar que el rol asignado sea efectivamente "TURISTA"
        userRepository.findByEmail("juan.perez@example.com").ifPresent(user -> {
            assertTrue(user.getRole().getName().equals("ROLE_TURISTA"));
        });
    }
}
