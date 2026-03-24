package com.travel.controllers;

import com.travel.dto.BookingRequest;
import com.travel.dto.BookingResponseDTO;
import com.travel.model.FlightBooking;
import com.travel.model.Flight;
import com.travel.model.auth.User;
import com.travel.model.AuditLog;
import com.travel.repository.AuditLogRepository;
import com.travel.repository.FlightBookingRepository;
import com.travel.repository.FlightRepository;
import com.travel.repository.UserRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/flight-bookings")
@RequiredArgsConstructor
public class FlightBookingController {

    private final FlightBookingRepository flightBookingRepository;
    private final FlightRepository flightRepository;
    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;

    @PostMapping
    @Transactional
    public ResponseEntity<?> createBooking(@RequestBody BookingRequest request) {
        if (request.getReturnDate().isBefore(request.getDepartureDate())) {
            return ResponseEntity.badRequest().body("Error: La fecha de regreso no puede ser anterior a la de salida");
        }

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email;
        if (principal instanceof UserDetails) {
            email = ((UserDetails) principal).getUsername();
        } else {
            email = principal.toString();
        }

        User user = userRepository.findByEmail(email).orElseThrow();

        Flight flight = flightRepository.findById(request.getFlightId()).orElse(null);
        
        if (flight == null) {
            return ResponseEntity.notFound().build();
        }

        if (flight.getAvailableSeats() <= 0) {
            return ResponseEntity.badRequest().body("Lo sentimos, no hay cupos disponibles para este destino");
        }

        flight.setAvailableSeats(flight.getAvailableSeats() - 1);
        flightRepository.save(flight);

        String bookingCode = "TC-" + generateRandomString(5);

        FlightBooking booking = FlightBooking.builder()
                .bookingCode(bookingCode)
                .user(user)
                .flight(flight)
                .departureDate(request.getDepartureDate())
                .returnDate(request.getReturnDate())
                .status("Confirmada")
                .bookingPrice(flight.getBasePrice())
                .build();

        FlightBooking savedBooking = flightBookingRepository.save(booking);

        return ResponseEntity.ok(mapToDTO(savedBooking));
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchBookings(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String document,
            @RequestParam(required = false) String name) {
        
        List<FlightBooking> results = new ArrayList<>();

        if (email != null && !email.isEmpty()) {
            results = flightBookingRepository.findByUserEmail(email);
        } else if (document != null && !document.isEmpty()) {
            results = flightBookingRepository.findByUserDocument(document);
        } else if (name != null && !name.isEmpty()) {
            results = flightBookingRepository.findByUserFullNameContainingIgnoreCase(name);
        }

        if (results.isEmpty()) {
            return ResponseEntity.status(404).body("No se encontraron reservas con los datos ingresados");
        }

        List<BookingResponseDTO> dtos = results.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    private BookingResponseDTO mapToDTO(FlightBooking booking) {
        return BookingResponseDTO.builder()
                .id(booking.getId())
                .bookingCode(booking.getBookingCode())
                .flightCode(booking.getFlight().getFlightCode())
                .origin(booking.getFlight().getOrigin())
                .destination(booking.getFlight().getDestination())
                .departureDate(booking.getDepartureDate())
                .returnDate(booking.getReturnDate())
                .status(booking.getStatus())
                .isAdministrative(booking.getIsAdministrative())
                .bookingPrice(booking.getBookingPrice())
                .build();
    }

    private String generateRandomString(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<?> updateBooking(@PathVariable UUID id, @RequestBody FlightBooking details) {
        FlightBooking booking = flightBookingRepository.findById(id).orElse(null);
        if (booking == null) return ResponseEntity.notFound().build();

        if ("Ejecutada".equalsIgnoreCase(booking.getStatus()) || "Finalizada".equalsIgnoreCase(booking.getStatus())) {
            return ResponseEntity.badRequest().body("No se puede editar una reserva ya ejecutada");
        }

        String oldDetails = "Status: " + booking.getStatus();
        booking.setStatus(details.getStatus());
        booking.setDepartureDate(details.getDepartureDate());
        booking.setReturnDate(details.getReturnDate());

        FlightBooking saved = flightBookingRepository.save(booking);

        // Audit Log (CP-ADM-048)
        auditLogRepository.save(AuditLog.builder()
                .entityId(id.toString())
                .entityType("FlightBooking")
                .action("UPDATE")
                .details("Changes: " + oldDetails + " -> " + saved.getStatus())
                .performedBy(getCurrentUserEmail())
                .timestamp(LocalDateTime.now())
                .build());

        return ResponseEntity.ok(mapToDTO(saved));
    }

    private String getCurrentUserEmail() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) return ((UserDetails) principal).getUsername();
        return principal.toString();
    }

    @PostMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createAdminBooking(@RequestBody BookingRequest request) {
        ResponseEntity<?> response = createBooking(request);
        if (response.getStatusCode().is2xxSuccessful()) {
            Object body = response.getBody();
            if (body instanceof BookingResponseDTO) {
                BookingResponseDTO dto = (BookingResponseDTO) body;
                FlightBooking booking = flightBookingRepository.findById(dto.getId()).orElseThrow();
                booking.setIsAdministrative(true);
                flightBookingRepository.save(booking);
                return ResponseEntity.ok(booking);
            }
        }
        return response;
    }

    @PostMapping("/{id}/cancel")
    @Transactional
    public ResponseEntity<?> cancelBooking(@PathVariable UUID id, @RequestBody CancellationRequest request) {
        if (request.getJustification() == null || request.getJustification().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("El motivo de cancelación es obligatorio");
        }

        FlightBooking booking = flightBookingRepository.findById(id).orElse(null);
        if (booking == null) return ResponseEntity.notFound().build();

        if ("Cancelada".equalsIgnoreCase(booking.getStatus())) {
            return ResponseEntity.badRequest().body("La reserva ya está cancelada");
        }

        booking.setStatus("Cancelada");
        booking.setCancellationJustification(request.getJustification());
        
        // Liberate seats
        Flight flight = booking.getFlight();
        flight.setAvailableSeats(flight.getAvailableSeats() + 1);
        flightRepository.save(flight);

        flightBookingRepository.save(booking);

        // Audit Log
        auditLogRepository.save(AuditLog.builder()
                .entityId(id.toString())
                .entityType("FlightBooking")
                .action("CANCEL")
                .details("Justification: " + request.getJustification())
                .performedBy(getCurrentUserEmail())
                .timestamp(LocalDateTime.now())
                .build());

        return ResponseEntity.ok("Reserva anulada y cupos liberados");
    }

    @Data
    public static class CancellationRequest {
        private String justification;
    }
}
