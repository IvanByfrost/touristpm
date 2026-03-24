package com.travel.controllers.catalog;

import com.travel.model.AuditLog;
import com.travel.model.catalog.Rate;
import com.travel.repository.AuditLogRepository;
import com.travel.repository.catalog.RateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/rates")
@RequiredArgsConstructor
public class RateController {

    private final RateRepository rateRepository;
    private final AuditLogRepository auditLogRepository;
    private final com.travel.repository.UserRepository userRepository;

    @GetMapping
    public List<Rate> getAll() {
        return rateRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Rate> getById(@PathVariable UUID id) {
        return rateRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Rate create(@RequestBody Rate rate) {
        return rateRepository.save(rate);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> update(@PathVariable UUID id, @RequestBody Rate details) {
        if (details.getAmount() == null || details.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return ResponseEntity.badRequest().body("El valor de la tarifa debe ser superior a cero");
        }

        return rateRepository.findById(id).map(rate -> {
            BigDecimal oldAmount = rate.getAmount();
            rate.setDescription(details.getDescription());
            rate.setAmount(details.getAmount());
            rate.setServiceType(details.getServiceType());
            
            // Fetch current user
            String email = getCurrentUserEmail();
            com.travel.model.auth.User user = userRepository.findByEmail(email).orElse(null);
            rate.setUpdatedBy(user);
            
            Rate saved = rateRepository.save(rate);

            auditLogRepository.save(AuditLog.builder()
                    .entityId(id.toString())
                    .entityType("Rate")
                    .action("UPDATE")
                    .details("Price changed: " + oldAmount + " -> " + saved.getAmount())
                    .performedBy(email)
                    .timestamp(LocalDateTime.now())
                    .build());

            return ResponseEntity.ok(saved);
        }).orElse(ResponseEntity.notFound().build());
    }

    private String getCurrentUserEmail() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) return ((UserDetails) principal).getUsername();
        return principal.toString();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        return rateRepository.findById(id).map(rate -> {
            rateRepository.delete(rate);
            return ResponseEntity.noContent().<Void>build();
        }).orElse(ResponseEntity.notFound().build());
    }
}