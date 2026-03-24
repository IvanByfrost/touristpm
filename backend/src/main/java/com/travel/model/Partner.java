package com.travel.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "partners")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Partner {
    @Id
    @Column(name = "partner_id", length = 50)
    private String partnerId; // NIT or SOC-123

    @Column(nullable = false, length = 100)
    private String companyName;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false, length = 20)
    private String phone;

    @Builder.Default
    @Column(length = 20)
    private String status = "Activo";
}
