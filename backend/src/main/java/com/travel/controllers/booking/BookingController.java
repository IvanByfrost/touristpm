package com.travel.controllers.booking;

import com.travel.dto.booking.BookingRequestDTO;
import com.travel.model.booking.Booking;
import com.travel.model.auth.User;
import com.travel.repository.BookingRepository;
import com.travel.repository.UserRepository;
import com.travel.repository.catalog.PackageRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import com.travel.repository.AuditLogRepository;
import java.time.LocalDateTime;
import java.math.RoundingMode;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final PackageRepository packageRepository;
    private final AuditLogRepository auditLogRepository;

    @GetMapping
    public List<Booking> getAll() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin) {
            System.out.println("ADMIN Request: Listing all bookings");
            return bookingRepository.findAll();
        }

        return userRepository.findByEmail(email)
                .map(user -> bookingRepository.findByUser(user))
                .orElse(Collections.emptyList());
    }

    // CP-TUR-021: Consulta por correo electrónico o documento
    @GetMapping("/search")
    public List<Booking> search(@RequestParam(required = false) String email, @RequestParam(required = false) String document) {
        System.out.println("--- CONSULTANDO RESERVAS ---");
        
        Optional<User> userOpt = Optional.empty();
        if (email != null && !email.isEmpty()) {
            System.out.println("Buscando por email: " + email);
            userOpt = userRepository.findByEmail(email);
        } else if (document != null && !document.isEmpty()) {
            System.out.println("Buscando por documento: " + document);
            userOpt = userRepository.findByDocument(document);
        }

        return userOpt.map(user -> {
            List<Booking> results = bookingRepository.findByUser(user);
            System.out.println("Encontradas " + results.size() + " reservas.");
            return results;
        }).orElseGet(() -> {
            System.out.println("Usuario no encontrado.");
            return Collections.emptyList();
        });
    }

    @GetMapping("/{id}")
    public ResponseEntity<Booking> getById(@PathVariable UUID id) {
        return bookingRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody BookingRequestDTO request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Booking booking = new Booking();
        booking.setUser(user);
        booking.setBookingType(request.getBookingType());
        if (request.getTotalAmount() != null) {
            if (request.getTotalAmount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
                return ResponseEntity.badRequest().body("El valor de la tarifa debe ser superior a cero");
            }
            booking.setTotalAmount(request.getTotalAmount().setScale(2, RoundingMode.HALF_UP));
        }
        booking.setDetails(request.getDetails());
        
        // CP-TUR-018: Validación de coherencia en fechas
        if (request.getDepartureDate() != null && request.getReturnDate() != null) {
            if (request.getReturnDate().isBefore(request.getDepartureDate())) {
                throw new RuntimeException("La fecha de regreso no puede ser anterior a la de salida");
            }
            booking.setDepartureDate(request.getDepartureDate());
            booking.setReturnDate(request.getReturnDate());
        }

        if (request.getPackageId() != null) {
            packageRepository.findById(request.getPackageId()).ifPresent(p -> {
                int requestedQty = (request.getQuantity() != null) ? request.getQuantity() : 1;
                
                // CP-TUR-017: Verificar y descontar cupos
                if (p.getAvailableSlots() != null) {
                    if (p.getAvailableSlots() < requestedQty) {
                        throw new RuntimeException("No hay suficientes cupos disponibles (" + p.getAvailableSlots() + " restantes)");
                    }
                    p.setAvailableSlots(p.getAvailableSlots() - requestedQty);
                    packageRepository.save(p);
                }
                
                booking.setTravelPackage(p);
            });
        }

        return ResponseEntity.ok(bookingRepository.save(booking));
    }

    @PutMapping("/{id}/confirm")
    public ResponseEntity<Booking> confirm(@PathVariable UUID id) {
        return bookingRepository.findById(id).map(booking -> {
            booking.setStatus("Confirmed");
            return ResponseEntity.ok(bookingRepository.save(booking));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<Booking> cancel(@PathVariable UUID id, @RequestParam String reason) {
        return bookingRepository.findById(id).map(booking -> {
            booking.setStatus("Cancelled");
            booking.setCancellationReason(reason);
            booking.setCancellationDate(LocalDateTime.now());
            
            // Recuperar cupos si tiene paquete asignado
            if (booking.getTravelPackage() != null) {
                var pkg = booking.getTravelPackage();
                if (pkg.getAvailableSlots() != null) {
                    pkg.setAvailableSlots(pkg.getAvailableSlots() + 1);
                    packageRepository.save(pkg);
                }
            }
            
            // Auditoría
            var auth = SecurityContextHolder.getContext().getAuthentication();
            auditLogRepository.save(com.travel.model.AuditLog.builder()
                .action("CANCEL_BOOKING")
                .entityType("Booking")
                .entityId(booking.getBookingId().toString())
                .performedBy(auth != null ? auth.getName() : "System")
                .details("Anulación admin. Motivo: " + reason)
                .timestamp(LocalDateTime.now())
                .build());

            return ResponseEntity.ok(bookingRepository.save(booking));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable UUID id, @RequestBody Booking details) {
        return bookingRepository.findById(id).map(booking -> {
            booking.setStatus(details.getStatus());
            booking.setDetails(details.getDetails());
            if (details.getTotalAmount() != null) {
                if (details.getTotalAmount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
                    return ResponseEntity.badRequest().body("El valor de la tarifa debe ser superior a cero");
                }
                booking.setTotalAmount(details.getTotalAmount().setScale(2, RoundingMode.HALF_UP));
            }
            return ResponseEntity.ok(bookingRepository.save(booking));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        return bookingRepository.findById(id).map(booking -> {
            bookingRepository.delete(booking);
            return ResponseEntity.noContent().<Void>build();
        }).orElse(ResponseEntity.notFound().build());
    }
}
