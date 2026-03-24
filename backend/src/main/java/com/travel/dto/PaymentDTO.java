package com.travel.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDTO {
    private UUID paymentId;
    private UUID bookingId;
    private String maskedCardNumber;
    private BigDecimal amountPaid;
    private String paymentStatus;
    private String receiptUrl;
    private LocalDateTime paymentDate;
}
