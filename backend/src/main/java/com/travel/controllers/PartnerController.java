package com.travel.controllers;

import com.travel.model.Partner;
import com.travel.repository.PartnerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/partners")
@RequiredArgsConstructor
public class PartnerController {

    private final PartnerRepository partnerRepository;

    @PostMapping
    public ResponseEntity<?> createPartner(@RequestBody @Valid Partner partner) {
        if (partnerRepository.existsById(partner.getPartnerId())) {
            return ResponseEntity.badRequest().body("Socio ya existente");
        }
        
        if (partner.getCompanyName() == null || partner.getCompanyName().trim().isEmpty() ||
            partner.getPhone() == null || partner.getPhone().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Todos los campos son obligatorios");
        }

        return ResponseEntity.ok(partnerRepository.save(partner));
    }

    @GetMapping("/search")
    public List<Partner> searchPartners(@RequestParam String query) {
        return partnerRepository.searchByIdOrName(query);
    }

    @GetMapping
    public List<Partner> getAll() {
        return partnerRepository.findAll();
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updatePartner(@PathVariable String id, @RequestBody Partner details) {
        return partnerRepository.findById(id).map(partner -> {
            if (details.getPhone() == null || details.getPhone().trim().isEmpty() ||
                details.getAddress() == null || details.getAddress().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Todos los campos son obligatorios");
            }
            partner.setPhone(details.getPhone());
            partner.setAddress(details.getAddress());
            partner.setCompanyName(details.getCompanyName());
            return ResponseEntity.ok(partnerRepository.save(partner));
        }).orElse(ResponseEntity.notFound().build());
    }
}
