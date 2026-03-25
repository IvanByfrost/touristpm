package com.travel.controllers.catalog;

import com.travel.model.catalog.Package;
import com.travel.repository.catalog.PackageRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.RoundingMode;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/packages")
@RequiredArgsConstructor
public class PackageController {

    private final PackageRepository packageRepository;

    @GetMapping
    public List<Package> getAll() {
        return packageRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Package> getById(@PathVariable UUID id) {
        return packageRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Package create(@RequestBody Package travelPackage) {
        return packageRepository.save(travelPackage);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable UUID id, @RequestBody Package details) {
        return packageRepository.findById(id).map(p -> {
            p.setName(details.getName());
            p.setDescription(details.getDescription());
            p.setDestination(details.getDestination());
            p.setAccommodation(details.getAccommodation());
            p.setTransport(details.getTransport());
            
            if (details.getTotalPrice() != null) {
                if (details.getTotalPrice().compareTo(java.math.BigDecimal.ZERO) <= 0) {
                    return ResponseEntity.badRequest().body("El valor de la tarifa debe ser superior a cero");
                }
                p.setTotalPrice(details.getTotalPrice().setScale(2, RoundingMode.HALF_UP));
            }
            p.setAvailableSlots(details.getAvailableSlots());
            p.setIsActive(details.getIsActive());
            p.setStartDate(details.getStartDate());
            p.setEndDate(details.getEndDate());
            return ResponseEntity.ok(packageRepository.save(p));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        return packageRepository.findById(id).map(p -> {
            packageRepository.delete(p);
            return ResponseEntity.noContent().<Void>build();
        }).orElse(ResponseEntity.notFound().build());
    }
}