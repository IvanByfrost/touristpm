package com.travel.controllers.finance;

import com.travel.dto.PaymentDTO;
import com.travel.model.finance.Payment;
import com.travel.repository.finance.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentRepository paymentRepository;

    @GetMapping
    public List<PaymentDTO> getAll() {
        return paymentRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentDTO> getById(@PathVariable UUID id) {
        return paymentRepository.findById(id)
                .map(p -> ResponseEntity.ok(mapToDTO(p)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public PaymentDTO processPayment(@RequestBody Payment payment) {
        // En un sistema real, aquí llamarías a una pasarela como Stripe o PayPal
        Payment saved = paymentRepository.save(payment);
        return mapToDTO(saved);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<PaymentDTO> updateStatus(@PathVariable UUID id, @RequestParam String status) {
        return paymentRepository.findById(id).map(p -> {
            p.setPaymentStatus(status);
            Payment saved = paymentRepository.save(p);
            return ResponseEntity.ok(mapToDTO(saved));
        }).orElse(ResponseEntity.notFound().build());
    }

    private PaymentDTO mapToDTO(Payment p) {
        String num = p.getPaymentMethod().getCardNumber() != null ? 
                p.getPaymentMethod().getCardNumber().replaceAll("\\s+", "") : "";
        String lastFour = num.length() >= 4 ? num.substring(num.length() - 4) : num;
        String masked = "**** **** **** " + lastFour;

        return PaymentDTO.builder()
                .paymentId(p.getPaymentId())
                .bookingId(p.getBooking().getBookingId())
                .maskedCardNumber(masked)
                .amountPaid(p.getAmountPaid())
                .paymentStatus(p.getPaymentStatus())
                .receiptUrl(p.getReceiptUrl())
                .paymentDate(p.getPaymentDate())
                .build();
    }
}