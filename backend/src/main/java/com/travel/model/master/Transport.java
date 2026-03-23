package com.travel.model.master;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "transports")
public class Transport {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "transport_id", columnDefinition = "BINARY(16)")
    private UUID transportId;

    @Column(name = "transport_type", length = 50)
    private String transportType;

    @Column(name = "provider_company", length = 100)
    private String providerCompany;

    @Column(name = "max_capacity")
    private Integer maxCapacity;
}