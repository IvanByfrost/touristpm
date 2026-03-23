package com.travel.controllers.finance;

import com.travel.model.finance.PaymentMethod;
import com.travel.repository.finance.PaymentMethodRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/payment-methods")
@RequiredArgsConstructor
public class PaymentMethodController {

    private final PaymentMethodRepository paymentMethodRepository;

    @GetMapping
    public List<PaymentMethod> getAll() {
        return paymentMethodRepository.findAll();
    }

    @PostMapping
    public PaymentMethod create(@RequestBody PaymentMethod method) {
        return paymentMethodRepository.save(method);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        return paymentMethodRepository.findById(id).map(method -> {
            method.setIsActive(false);
            paymentMethodRepository.save(method);
            return ResponseEntity.noContent().<Void>build();
        }).orElse(ResponseEntity.notFound().build());
    }
}