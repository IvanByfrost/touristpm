package com.travel.model.booking;

import com.travel.model.auth.User;
import com.travel.model.catalog.Package;
import com.travel.model.catalog.Rate;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
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
    @JoinColumn(name = "package_id") // Es opcional
    private Package travelPackage;

    @ManyToOne
    @JoinColumn(name = "rate_id") // Es opcional
    private Rate rate;

    @Column(name = "booking_type", length = 50)
    private String bookingType;

    @Builder.Default
    @Column(name = "status", length = 20)
    private String status = "Pending";

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(columnDefinition = "TEXT")
    private String details;

    @Column(name = "booking_date", updatable = false)
    private LocalDateTime bookingDate;

    @PrePersist
    protected void onCreate() {
        bookingDate = LocalDateTime.now();
    }
}