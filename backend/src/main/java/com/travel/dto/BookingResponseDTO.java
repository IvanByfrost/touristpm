package com.travel.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponseDTO {
    private UUID id;
    private String bookingCode;
    private String flightCode;
    private String origin;
    private String destination;
    private LocalDateTime departureDate;
    private LocalDateTime returnDate;
    private String status;
    private Boolean isAdministrative;
    private BigDecimal bookingPrice;
}
