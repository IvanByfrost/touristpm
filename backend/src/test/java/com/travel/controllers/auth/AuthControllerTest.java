package com.travel.controllers.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.travel.dto.auth.SignupRequest;
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
    
    @Autowired
    private FlightBookingRepository flightBookingRepository;
    
    @Autowired
    private FlightRepository flightRepository;

    @BeforeEach
    void setUp() {
        flightBookingRepository.deleteAll();
        flightRepository.deleteAll();
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
        signupRequest.setFullName("Turista Prueba");
        signupRequest.setDocument("12345678");
        signupRequest.setEmail("turista@test.com");
        signupRequest.setPassword("password123");
        signupRequest.setRole(new HashSet<>(Collections.singletonList("ROLE_TURISTA")));

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isOk());

        // Post-condiciones: El nuevo usuario tiene credenciales válidas en la BD
        assertTrue(userRepository.findByEmail("turista@test.com").isPresent());
        
        // Notas: Verificar que el rol asignado sea efectivamente "TURISTA"
        userRepository.findByEmail("turista@test.com").ifPresent(user -> {
            assertTrue(user.getRole().getName().equals("ROLE_TURISTA"));
        });
    }

    @Test
    void testRegisterDuplicateEmail() throws Exception {
        // Escenario: CP-TUR-002 - Restricción de registro con correo duplicado
        
        // Pre-condición: El correo "duplicado@test.com" ya debe estar registrado
        SignupRequest firstRequest = new SignupRequest();
        firstRequest.setFullName("Usuario Original");
        firstRequest.setDocument("87654321");
        firstRequest.setEmail("duplicado@test.com");
        firstRequest.setPassword("password123");

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(firstRequest)))
                .andExpect(status().isOk());

        // Paso: Intentar registrar el mismo correo
        SignupRequest duplicateRequest = new SignupRequest();
        duplicateRequest.setFullName("Usuario Duplicado");
        duplicateRequest.setDocument("87654321");
        duplicateRequest.setEmail("duplicado@test.com");
        duplicateRequest.setPassword("otraPassword123");

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicateRequest)))
                .andExpect(status().isBadRequest());
        
        // Post-condiciones: No se crea ningún registro nuevo en la base de datos (se mantiene solo 1)
        assertTrue(userRepository.findAll().size() == 1);
    }

    @Test
    void testRegisterBlankPassword() throws Exception {
        // Escenario: CP-TUR-003 - Validación de contraseña obligatoria
        
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setFullName("Usuario Sin Clave");
        signupRequest.setDocument("11223344");
        signupRequest.setEmail("sinclave@test.com");
        signupRequest.setPassword(""); // Pasos: Dejar la contraseña en blanco

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isBadRequest()); // Resultados: El sistema bloquea la acción

        // Post-condiciones: No se crea el registro en la BD
        assertTrue(userRepository.findByEmail("sinclave@test.com").isEmpty());
    }

    @Test
    void testRegisterDuplicateEmailTestCorreo() throws Exception {
        // Escenario: CP-TUR-004 - Bloqueo de registro por correo existente
        
        // Pre-condición: El correo test@correo.com debe existir previamente
        SignupRequest existingUser = new SignupRequest();
        existingUser.setFullName("User Existing");
        existingUser.setDocument("55667788");
        existingUser.setEmail("test@correo.com");
        existingUser.setPassword("password123");

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(existingUser)))
                .andExpect(status().isOk());

        // Pasos: Intentar registro con el mismo correo
        SignupRequest duplicateRequest = new SignupRequest();
        duplicateRequest.setFullName("User Duplicate Attempt");
        duplicateRequest.setDocument("55667788");
        duplicateRequest.setEmail("test@correo.com");
        duplicateRequest.setPassword("securepass");

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicateRequest)))
                .andExpect(status().isBadRequest()); // Resultados: El sistema no permite el registro.

        // Post-condiciones: Verificación de que no se insertó fila extra en la BD
        assertTrue(userRepository.findAll().stream().filter(u -> u.getEmail().equals("test@correo.com")).count() == 1);
    }

    @Test
    void testRegisterMissingPassword() throws Exception {
        // Escenario: CP-TUR-005 - Bloqueo de registro por contraseña ausente
        
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setFullName("No Password User");
        signupRequest.setDocument("99001122");
        signupRequest.setEmail("nopass@test.com");
        signupRequest.setPassword(null); // Pasos: Dejar el campo totalmente vacío (null)

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isBadRequest()); // Resultados: El botón no procesa y pide la contraseña

        // Post-condiciones: Base de datos limpia de registros incompletos
        assertTrue(userRepository.findByEmail("nopass@test.com").isEmpty());
    }
}
