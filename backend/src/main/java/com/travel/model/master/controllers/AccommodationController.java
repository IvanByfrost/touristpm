package com.travel.model.master.controllers;

import com.travel.model.master.Accommodation;
import com.travel.model.master.repository.AccommodationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/accommodations")
@RequiredArgsConstructor
public class AccommodationController {

    private final AccommodationRepository accommodationRepository;

    // Listar todos los alojamientos
    @GetMapping
    public List<Accommodation> getAll() {
        return accommodationRepository.findAll();
    }

    // Ver detalle de un alojamiento
    @GetMapping("/{id}")
    public ResponseEntity<Accommodation> getById(@PathVariable UUID id) {
        return accommodationRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Crear nuevo alojamiento
    @PostMapping
    public Accommodation create(@RequestBody Accommodation accommodation) {
        return accommodationRepository.save(accommodation);
    }

    // Actualizar datos del alojamiento
    @PutMapping("/{id}")
    public ResponseEntity<Accommodation> update(@PathVariable UUID id, @RequestBody Accommodation details) {
        return accommodationRepository.findById(id).map(acc -> {
            acc.setName(details.getName());
            acc.setStars(details.getStars());
            acc.setAddress(details.getAddress());
            acc.setDestination(details.getDestination());
            return ResponseEntity.ok(accommodationRepository.save(acc));
        }).orElse(ResponseEntity.notFound().build());
    }

    // Eliminar alojamiento
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        return accommodationRepository.findById(id).map(acc -> {
            accommodationRepository.delete(acc);
            return ResponseEntity.noContent().<Void>build();
        }).orElse(ResponseEntity.notFound().build());
    }
}