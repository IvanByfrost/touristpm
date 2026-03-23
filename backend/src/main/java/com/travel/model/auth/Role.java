package com.travel.model.auth;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

import org.springframework.security.core.GrantedAuthority;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "roles")
public class Role implements GrantedAuthority {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "role_id", columnDefinition = "BINARY(16)")
    private UUID roleId;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Override
    public String getAuthority() {
        return name;
    }
}