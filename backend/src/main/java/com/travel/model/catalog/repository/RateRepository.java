package com.travel.model.catalog.repository;

import com.travel.model.catalog.Rate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface RateRepository extends JpaRepository<Rate, UUID> {
}