package com.travel.controllers.master;

import com.travel.model.master.Destination;
import com.travel.repository.master.DestinationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.travel.repository.AuditLogRepository;
import java.time.LocalDateTime;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/destinations")
@RequiredArgsConstructor
public class DestinationController {

    private final DestinationRepository destinationRepository;
    private final AuditLogRepository auditLogRepository;

    @GetMapping
    public List<Destination> getAll() {
        return destinationRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Destination> getById(@PathVariable UUID id) {
        return destinationRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Destination create(@RequestBody Destination destination) {
        return destinationRepository.save(destination);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Destination> update(@PathVariable UUID id, @RequestBody Destination details) {
        return destinationRepository.findById(id).map(dest -> {
            java.math.BigDecimal oldPrice = dest.getBasePrice();
            
            dest.setName(details.getName());
            dest.setCountry(details.getCountry());
            dest.setDescription(details.getDescription());
            dest.setBasePrice(details.getBasePrice());
            dest.setTaxPercentage(details.getTaxPercentage());
            
            Destination saved = destinationRepository.save(dest);

            // Auditoría si cambió el precio
            if (details.getBasePrice() != null) {
                String email = getCurrentUserEmail();
                auditLogRepository.save(com.travel.model.AuditLog.builder()
                        .entityId(id.toString())
                        .entityType("Destination")
                        .action("UPDATE_FEE")
                        .details("Tarifa actualizada: " + (oldPrice != null ? oldPrice : "0") + " -> " + saved.getBasePrice() + " (IVA: " + saved.getTaxPercentage() + "%)")
                        .performedBy(email)
                        .timestamp(LocalDateTime.now())
                        .build());
            }

            return ResponseEntity.ok(saved);
        }).orElse(ResponseEntity.notFound().build());
    }

    private String getCurrentUserEmail() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return "System";
        Object principal = auth.getPrincipal();
        if (principal instanceof UserDetails) return ((UserDetails) principal).getUsername();
        return principal.toString();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        return destinationRepository.findById(id).map(dest -> {
            destinationRepository.delete(dest);
            return ResponseEntity.noContent().<Void>build();
        }).orElse(ResponseEntity.notFound().build());
    }
}