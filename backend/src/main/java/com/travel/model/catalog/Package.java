package com.travel.model.catalog;

import com.travel.model.master.Accommodation;
import com.travel.model.master.Destination;
import com.travel.model.master.Transport;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "packages")
public class Package {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "package_id", columnDefinition = "BINARY(16)")
    private UUID packageId;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne
    @JoinColumn(name = "destination_id")
    private Destination destination;

    @ManyToOne
    @JoinColumn(name = "accommodation_id")
    private Accommodation accommodation;

    @ManyToOne
    @JoinColumn(name = "transport_id")
    private Transport transport;

    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @Column(name = "available_slots")
    private Integer availableSlots;

    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = true;

    private LocalDate startDate;
    private LocalDate endDate;
}