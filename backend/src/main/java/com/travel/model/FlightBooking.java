package com.travel.model;

import com.travel.model.auth.User;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "flight_bookings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlightBooking {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String bookingCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flight_id", nullable = false)
    private Flight flight;

    @Column(nullable = false)
    private LocalDateTime departureDate;

    @Column(nullable = false)
    private LocalDateTime returnDate;

    @Column(name = "status", length = 20)
    private String status;

    @Builder.Default
    @Column(name = "is_administrative")
    private Boolean isAdministrative = false;

    @Column(name = "booking_price")
    private BigDecimal bookingPrice;

    @Column(name = "cancellation_justification")
    private String cancellationJustification;
}
