package com.travel.model.finance;

import com.travel.model.auth.User;
import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "payment_methods")
public class PaymentMethod {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "payment_method_id", columnDefinition = "BINARY(16)")
    private UUID paymentMethodId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "method_type", length = 50)
    private String methodType;

    @Column(name = "card_number", length = 20)
    private String cardNumber;

    @Column(name = "holder_name", length = 100)
    private String holderName;

    @Column(name = "expiration_date", length = 10)
    private String expirationDate;

    @Column(name = "encrypted_data", columnDefinition = "TEXT")
    private String encryptedData;

    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = true;
}