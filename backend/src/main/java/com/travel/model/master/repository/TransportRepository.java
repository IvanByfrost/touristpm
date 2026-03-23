package com.travel.model.master.repository;

import com.travel.model.master.Transport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface TransportRepository extends JpaRepository<Transport, UUID> {
}