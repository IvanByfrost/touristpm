package com.travel.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class BookingRequest {
    private UUID flightId;
    private LocalDateTime departureDate;
    private LocalDateTime returnDate;
}
