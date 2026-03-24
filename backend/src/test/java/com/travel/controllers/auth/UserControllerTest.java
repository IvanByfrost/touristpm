package com.travel.controllers.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.travel.dto.auth.DeleteAccountRequest;
import com.travel.dto.auth.JwtResponse;
import com.travel.dto.auth.LoginRequest;
import com.travel.dto.auth.SignupRequest;
import com.travel.model.auth.Role;
import com.travel.model.auth.User;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private String jwtToken;
    private UUID userId;

    @BeforeEach
    void setUp() throws Exception {
        userRepository.deleteAll();
        roleRepository.deleteAll();
        
        roleRepository.save(Role.builder().name("ROLE_TURISTA").build());

        // 1. Registrar usuario
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setFullName("Juan Original");
        signupRequest.setDocument("12345678");
        signupRequest.setEmail("juan@test.com");
        signupRequest.setPassword("password123");
        signupRequest.setRole(new HashSet<>(Collections.singletonList("ROLE_TURISTA")));

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isOk());

        // 2. Login para obtener JWT
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("juan@test.com");
        loginRequest.setPassword("password123");

        MvcResult result = mockMvc.perform(post("/api/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        JwtResponse jwtResponse = objectMapper.readValue(response, JwtResponse.class);
        this.jwtToken = jwtResponse.getToken();
        this.userId = jwtResponse.getId();
    }

    @Test
    void testUpdateUserProfileSuccess() throws Exception {
        // Escenario: CP-TUR-006 - Actualización exitosa de datos personales
        
        Map<String, Object> updateDetails = new HashMap<>();
        updateDetails.put("fullName", "Juan Modificado");
        updateDetails.put("document", "12345678");
        updateDetails.put("email", "juan.nuevo@test.com");

        mockMvc.perform(put("/api/users/" + userId)
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDetails)))
                .andExpect(status().isOk());

        // Post-condiciones: Los nuevos datos persisten en la base de datos
        User updatedUser = userRepository.findById(userId).orElseThrow();
        assertEquals("Juan Modificado", updatedUser.getFullName());
        assertEquals("juan.nuevo@test.com", updatedUser.getEmail());
    }

    @Test
    void testUpdateUserDuplicateEmail() throws Exception {
        // Escenario: CP-TUR-007 - Bloqueo de actualización por email duplicado
        
        // Pre-condición: Existe otro usuario con yaexiste@test.com
        User otherUser = User.builder()
                .fullName("Otro Usuario")
                .document("98765432")
                .email("yaexiste@test.com")
                .password("password123")
                .role(roleRepository.findByName("ROLE_TURISTA").get())
                .isActive(true)
                .build();
        userRepository.save(otherUser);

        // Paso: Cambiar el email actual por el ya existente
        Map<String, Object> updateDetails = new HashMap<>();
        updateDetails.put("fullName", "Juan Intento");
        updateDetails.put("document", "12345678");
        updateDetails.put("email", "yaexiste@test.com");

        mockMvc.perform(put("/api/users/" + userId)
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDetails)))
                .andExpect(status().isBadRequest()); // Resultados: El sistema no actualiza y lanza alerta

        // Post-condiciones: Los datos del usuario permanecen intactos
        User originalUser = userRepository.findById(userId).orElseThrow();
        assertEquals("Juan Original", originalUser.getFullName());
        assertEquals("juan@test.com", originalUser.getEmail());
    }

    @Test
    void testUpdateUserMissingFields() throws Exception {
        // Escenario: CP-TUR-008 - Bloqueo por eliminación de campos obligatorios
        
        // Paso: Borrar el contenido del campo "Nombre" (fullName)
        Map<String, Object> updateDetails = new HashMap<>();
        updateDetails.put("fullName", ""); // Vacío
        updateDetails.put("document", "12345678");
        updateDetails.put("email", "juan@test.com");

        mockMvc.perform(put("/api/users/" + userId)
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDetails)))
                .andExpect(status().isBadRequest()); // Resultados: El sistema no guarda

        // Post-condiciones: Los datos originales permanecen
        User originalUser = userRepository.findById(userId).orElseThrow();
        assertEquals("Juan Original", originalUser.getFullName());
    }

    @Test
    void testUpdateUserMissingNameAndDocument() throws Exception {
        // Escenario: CP-TUR-009 - Bloqueo por eliminación de datos obligatorios
        
        // Paso: Borrar la información de los campos "Nombre" (fullName) y "Documento"
        Map<String, Object> updateDetails = new HashMap<>();
        updateDetails.put("fullName", "");
        updateDetails.put("document", "");
        updateDetails.put("email", "juan@test.com");

        mockMvc.perform(put("/api/users/" + userId)
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDetails)))
                .andExpect(status().isBadRequest()); // Resultados: El sistema no actualiza la BD

        // Post-condiciones: La información del turista permanece idéntica a la original
        User originalUser = userRepository.findById(userId).orElseThrow();
        assertEquals("Juan Original", originalUser.getFullName());
        assertEquals("12345678", originalUser.getDocument());
    }

    @Test
    void testUpdateUserDuplicateEmailSpecific() throws Exception {
        // Escenario: CP-TUR-010 - Restricción de cambio a email ya registrado
        
        // Pre-condición: Existe otro usuario (con otro rol, e.g., ROLE_USER) con perfil_duplicado@test.com
        roleRepository.save(Role.builder().name("ROLE_USER").build());
        User otherUser = User.builder()
                .fullName("Otro Admin")
                .document("99999999")
                .email("perfil_duplicado@test.com")
                .password("password123")
                .role(roleRepository.findByName("ROLE_USER").get())
                .isActive(true)
                .build();
        userRepository.save(otherUser);

        // Paso: Ingresar el email duplicado en el campo correspondiente
        Map<String, Object> updateDetails = new HashMap<>();
        updateDetails.put("fullName", "Juan Update");
        updateDetails.put("document", "12345678");
        updateDetails.put("email", "perfil_duplicado@test.com");

        mockMvc.perform(put("/api/users/" + userId)
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDetails)))
                .andExpect(status().isBadRequest()); // Resultados: El sistema arroja el error

        // Post-condiciones: Integridad de cuenta mantenida
        User originalUser = userRepository.findById(userId).orElseThrow();
        assertEquals("juan@test.com", originalUser.getEmail());
    }

    @Test
    void testUpdateUserInvalidEmail() throws Exception {
        // Escenario: CP-TUR-011 - Validación de formato de email y caracteres
        
        // Paso: Ingresar un correo con formato inválido
        Map<String, Object> updateDetails = new HashMap<>();
        updateDetails.put("fullName", "Juan Format");
        updateDetails.put("document", "12345678");
        updateDetails.put("email", "email-sin-formato"); // Inválido

        mockMvc.perform(put("/api/users/" + userId)
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDetails)))
                .andExpect(status().isBadRequest()); // Resultados: El sistema bloquea la actualización

        // Post-condiciones: No hay alteración en la base de datos
        User originalUser = userRepository.findById(userId).orElseThrow();
        assertEquals("juan@test.com", originalUser.getEmail());
    }

    @Test
    void testDeleteUserAccount() throws Exception {
        // Escenario: CP-TUR-012 - Ejecución completa de borrado de cuenta
        
        // Pasos: Presionar el botón "Inactivar cuenta" -> Confirmar -> Delete en BD
        DeleteAccountRequest deleteRequest = new DeleteAccountRequest();
        deleteRequest.setPassword("password123");

        mockMvc.perform(delete("/api/users/" + userId)
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(deleteRequest)))
                .andExpect(status().isNoContent()); // Resultados: El sistema borra el registro en BD (204)

        // Post-condiciones: El usuario ya no puede ingresar con esas credenciales
        assertTrue(userRepository.findById(userId).isEmpty());
    }

    @Test
    void testDeleteUserAccountInvalidPassword() throws Exception {
        // Escenario: CP-TUR-014 - Bloqueo por validación de identidad incorrecta
        
        // Pasos: Presionar el botón "Inactivar cuenta" -> Credencial incorrecta
        DeleteAccountRequest deleteRequest = new DeleteAccountRequest();
        deleteRequest.setPassword("password_erroneo");

        mockMvc.perform(delete("/api/users/" + userId)
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(deleteRequest)))
                .andExpect(status().isUnauthorized()); // Resultados: El sistema deniega el borrado

        // Post-condiciones: El registro original permanece intacto
        assertTrue(userRepository.findById(userId).isPresent());
    }

    @Test
    void testDeleteUserAccountSecurityCancelled() throws Exception {
        // Escenario: CP-TUR-015 - Mantenimiento de cuenta al cancelar validación
        
        // Pasos: El usuario está en "Inactivar" -> Paso de seguridad -> "Cancelar"
        // (En el backend, esto significa que tras consultar el usuario, no se procede con el DELETE)
        
        // 1. Simular que el frontend pide los datos antes de borrar (GET)
        mockMvc.perform(get("/api/users/" + userId)
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk());

        // 2. El usuario cancela en el UI (No se hace ninguna petición más de borrado)

        // Post-condiciones: El usuario mantiene su acceso y los datos siguen ahí
        assertTrue(userRepository.findById(userId).isPresent());
        
        // Verificar que el login sigue funcionando perfectamente
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("juan@test.com");
        loginRequest.setPassword("password123");

        mockMvc.perform(post("/api/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk());
    }

    @Test
    void testDeleteUserAccountCancelled() throws Exception {
        // Escenario: CP-TUR-013 - Cancelación del proceso de borrado
        
        // Pasos: El usuario presiona "Inactivar" -> Modal -> "Cancelar"
        // (En el backend, esto significa que el usuario SIGUE existiendo porque el endpoint DELETE no se llamó)
        
        // Verificación inicial: El usuario existe
        assertTrue(userRepository.findById(userId).isPresent());

        // Simulación: Realizamos otra acción (ej. GET) en lugar de DELETE
        mockMvc.perform(get("/api/users/" + userId)
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk());

        // Post-condiciones: Los datos permanecen sin cambios
        assertTrue(userRepository.findById(userId).isPresent());
        User user = userRepository.findById(userId).orElseThrow();
        assertEquals("juan@test.com", user.getEmail());
    }
}
