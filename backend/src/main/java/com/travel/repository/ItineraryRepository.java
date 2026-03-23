package com.travel.repository;

import com.travel.model.booking.Itinerary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface ItineraryRepository extends JpaRepository<Itinerary, UUID> {
    List<Itinerary> findByBooking_BookingId(UUID bookingId);
}