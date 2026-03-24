package com.travel.repository;

import com.travel.model.FlightBooking;
import com.travel.model.auth.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface FlightBookingRepository extends JpaRepository<FlightBooking, UUID> {
    List<FlightBooking> findByUser(User user);
    List<FlightBooking> findByUserEmail(String email);
    List<FlightBooking> findByUserDocument(String document);
    List<FlightBooking> findByUserFullNameContainingIgnoreCase(String name);
}
