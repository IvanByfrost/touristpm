package com.travel.dto.booking;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingRequestDTO {
    private UUID packageId;
    private String bookingType;
    private BigDecimal totalAmount;
    private Integer quantity;
    private LocalDate departureDate;
    private LocalDate returnDate;
    private String details;
}
