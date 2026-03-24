package com.travel.model.booking;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "itineraries")
public class Itinerary {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "itinerary_id", columnDefinition = "BINARY(16)")
    private UUID itineraryId;

    @ManyToOne
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "activity_date")
    private java.time.LocalDateTime activityDate;
}
