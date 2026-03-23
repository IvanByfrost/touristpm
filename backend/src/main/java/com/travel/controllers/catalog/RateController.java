package com.travel.controllers.catalog;

import com.travel.model.catalog.Rate;
import com.travel.repository.catalog.RateRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/rates")
@RequiredArgsConstructor
public class RateController {

    private final RateRepository rateRepository;

    @GetMapping
    public List<Rate> getAll() {
        return rateRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Rate> getById(@PathVariable UUID id) {
        return rateRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Rate create(@RequestBody Rate rate) {
        return rateRepository.save(rate);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Rate> update(@PathVariable UUID id, @RequestBody Rate details) {
        return rateRepository.findById(id).map(rate -> {
            rate.setDescription(details.getDescription());
            rate.setAmount(details.getAmount());
            rate.setServiceType(details.getServiceType());
            rate.setUpdatedBy(details.getUpdatedBy());
            return ResponseEntity.ok(rateRepository.save(rate));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        return rateRepository.findById(id).map(rate -> {
            rateRepository.delete(rate);
            return ResponseEntity.noContent().<Void>build();
        }).orElse(ResponseEntity.notFound().build());
    }
}