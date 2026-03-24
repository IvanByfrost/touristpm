package com.travel.dto;

import lombok.*;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentMethodDTO {
    private UUID id;
    private String maskedCardNumber;
    private String holderName;
    private String expirationDate;
    private String cardType;
}
