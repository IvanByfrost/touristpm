package com.travel.controllers.auth;

import com.travel.dto.auth.DeleteAccountRequest;
import com.travel.model.auth.User;
import com.travel.dto.UserResponseDTO;
import com.travel.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping
    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserResponseDTO::fromEntity)
                .toList();
    }

    // 2. BUSCAR POR ID (GET)
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable UUID id) {
        return userRepository.findById(id)
                .map(UserResponseDTO::fromEntity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 3. CREAR (POST)
    @PostMapping
    public UserResponseDTO createUser(@RequestBody User user) {
        return UserResponseDTO.fromEntity(userRepository.save(user));
    }

    // 4. ACTUALIZAR (PUT)
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable UUID id, @Valid @RequestBody User userDetails) {
        return userRepository.findById(id).map(user -> {
            // Validar que el nuevo correo no esté en uso por otro usuario
            if (!user.getEmail().equals(userDetails.getEmail()) && 
                userRepository.findByEmail(userDetails.getEmail()).isPresent()) {
                return ResponseEntity.badRequest().body("Error: El correo ya está en uso");
            }

            user.setFullName(userDetails.getFullName());
            user.setDocument(userDetails.getDocument());
            user.setEmail(userDetails.getEmail());
            // No actualizamos el password así de fácil en la vida real, pero para el plan
            // sirve
            return ResponseEntity.ok(UserResponseDTO.fromEntity(userRepository.save(user)));
        }).orElse(ResponseEntity.notFound().build());
    }

    // 5. ELIMINAR (DELETE)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable UUID id, @RequestBody DeleteAccountRequest request) {
        return userRepository.findById(id).map(user -> {
            // CP-TUR-014: Validar identidad antes de borrar
            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Error: Fallo de seguridad");
            }
            userRepository.delete(user);
            return ResponseEntity.noContent().build();
        }).orElse(ResponseEntity.notFound().build());
    }
}