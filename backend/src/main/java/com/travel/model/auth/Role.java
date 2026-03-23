package com.travel.model.auth;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "roles")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "role_id", columnDefinition = "BINARY(16)")
    private UUID roleId;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

}