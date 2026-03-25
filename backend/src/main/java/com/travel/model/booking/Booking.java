package com.travel.model.booking;

import com.travel.model.auth.User;
import com.travel.model.catalog.Package;
import com.travel.model.catalog.Rate;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "booking_id", columnDefinition = "BINARY(16)")
    private UUID bookingId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "package_id")
    private Package travelPackage;

    @ManyToOne
    @JoinColumn(name = "rate_id")
    private Rate rate;

    @Column(name = "booking_type", length = 50)
    private String bookingType;

    @Column(name = "booking_code", length = 10, unique = true)
    private String bookingCode;

    @Builder.Default
    @Column(name = "status", length = 20)
    private String status = "Pending";

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(columnDefinition = "TEXT")
    private String details;

    @Column(name = "booking_date", updatable = false)
    private LocalDateTime bookingDate;

    @Column(name = "departure_date")
    private LocalDate departureDate;

    @Column(name = "return_date")
    private LocalDate returnDate;

    @Column(name = "cancellation_reason", length = 500)
    private String cancellationReason;

    @Column(name = "cancellation_date")
    private LocalDateTime cancellationDate;

    @PrePersist
    protected void onCreate() {
        bookingDate = LocalDateTime.now();
        if (bookingCode == null) {
            this.bookingCode = generateRandomCode();
        }
    }

    private String generateRandomCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder("TC-");
        java.util.Random rnd = new java.util.Random();
        for (int i = 0; i < 5; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
