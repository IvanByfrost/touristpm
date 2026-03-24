package com.travel.controllers.finance;

import com.travel.dto.PaymentMethodDTO;
import com.travel.model.auth.User;
import com.travel.model.finance.PaymentMethod;
import com.travel.repository.finance.PaymentMethodRepository;
import com.travel.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/payment-methods")
@RequiredArgsConstructor
public class PaymentMethodController {

    private final PaymentMethodRepository paymentMethodRepository;
    private final UserRepository userRepository;

    @GetMapping
    public List<PaymentMethodDTO> getAll() {
        User user = getAuthenticatedUser();
        return paymentMethodRepository.findByUser(user).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @PostMapping
    public PaymentMethodDTO create(@Valid @RequestBody PaymentMethod method) {
        User user = getAuthenticatedUser();
        method.setUser(user);
        PaymentMethod saved = paymentMethodRepository.save(method);
        return mapToDTO(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        return paymentMethodRepository.findById(id).map(method -> {
            method.setIsActive(false);
            paymentMethodRepository.save(method);
            return ResponseEntity.noContent().<Void>build();
        }).orElse(ResponseEntity.notFound().build());
    }

    private User getAuthenticatedUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email = (principal instanceof UserDetails) ? ((UserDetails) principal).getUsername() : principal.toString();
        return userRepository.findByEmail(email).orElseThrow();
    }

    private PaymentMethodDTO mapToDTO(PaymentMethod pm) {
        String num = pm.getCardNumber() != null ? pm.getCardNumber().replaceAll("\\s+", "") : "";
        String lastFour = num.length() >= 4 ? num.substring(num.length() - 4) : num;
        String masked = "**** **** **** " + lastFour;

        return PaymentMethodDTO.builder()
                .id(pm.getPaymentMethodId())
                .maskedCardNumber(masked)
                .holderName(pm.getHolderName())
                .expirationDate(pm.getExpirationDate())
                .cardType(pm.getMethodType())
                .build();
    }
}