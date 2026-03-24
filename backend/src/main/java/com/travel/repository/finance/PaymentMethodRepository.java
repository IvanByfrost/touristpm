package com.travel.repository.finance;

import com.travel.model.auth.User;
import com.travel.model.finance.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, UUID> {
    List<PaymentMethod> findByUser(User user);
    List<PaymentMethod> findByUser_UserIdAndIsActiveTrue(UUID userId);
}