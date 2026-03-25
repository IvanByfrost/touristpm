package com.travel.controllers.booking;

import com.travel.model.AuditLog;
import com.travel.model.booking.Itinerary;
import com.travel.repository.AuditLogRepository;
import com.travel.repository.ItineraryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/itineraries")
@RequiredArgsConstructor
public class ItineraryController {

    private final ItineraryRepository itineraryRepository;
    private final AuditLogRepository auditLogRepository;

    @GetMapping("/booking/{bookingId}")
    public List<Itinerary> getByBooking(@PathVariable UUID bookingId) {
        System.out.println("--- FETCHING ITINERARY FOR: " + bookingId);
        return itineraryRepository.findByBooking_BookingId(bookingId);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateItinerary(@PathVariable UUID id, @RequestBody Itinerary details) {
        return itineraryRepository.findById(id).map(itinerary -> {
            String oldDesc = itinerary.getDescription();
            itinerary.setDescription(details.getDescription());
            Itinerary updated = itineraryRepository.save(itinerary);

            // Generar Log de Auditoría (CP-ADM-048)
            AuditLog log = AuditLog.builder()
                    .entityId(id.toString())
                    .entityType("ITINERARY")
                    .action("UPDATE")
                    .details(String.format("Desc cambiada: '%s' -> '%s'", oldDesc, details.getDescription()))
                    .performedBy("ADMIN") // En prod usar SecurityContextHolder.getContext().getAuthentication().getName()
                    .timestamp(LocalDateTime.now())
                    .build();
            auditLogRepository.save(log);

            return ResponseEntity.ok(updated);
        }).orElse(ResponseEntity.notFound().build());
    }
}
