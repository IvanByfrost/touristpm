package com.travel.controllers.finance;

import com.travel.model.finance.Payment;
import com.travel.repository.finance.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentRepository paymentRepository;

    @GetMapping
    public List<Payment> getAll() {
        return paymentRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Payment> getById(@PathVariable UUID id) {
        return paymentRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Payment processPayment(@RequestBody Payment payment) {
        // En un sistema real, aquí llamarías a una pasarela como Stripe o PayPal
        return paymentRepository.save(payment);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Payment> updateStatus(@PathVariable UUID id, @RequestParam String status) {
        return paymentRepository.findById(id).map(p -> {
            p.setPaymentStatus(status);
            return ResponseEntity.ok(paymentRepository.save(p));
        }).orElse(ResponseEntity.notFound().build());
    }
}