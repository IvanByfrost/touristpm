package com.travel.repository;

import com.travel.model.Partner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PartnerRepository extends JpaRepository<Partner, String> {
    
    @Query("SELECT p FROM Partner p WHERE LOWER(p.partnerId) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(p.companyName) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Partner> searchByIdOrName(@Param("query") String query);
}
