package com.travel.controllers;

import com.travel.model.master.Destination;
import com.travel.repository.DestinationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/destinations")
@RequiredArgsConstructor
public class DestinationController {

    private final DestinationRepository destinationRepository;

    @GetMapping
    public List<Destination> getAll() {
        return destinationRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Destination> getById(@PathVariable UUID id) {
        return destinationRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Destination create(@RequestBody Destination destination) {
        return destinationRepository.save(destination);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Destination> update(@PathVariable UUID id, @RequestBody Destination details) {
        return destinationRepository.findById(id).map(dest -> {
            dest.setName(details.getName());
            dest.setCountry(details.getCountry());
            dest.setDescription(details.getDescription());
            return ResponseEntity.ok(destinationRepository.save(dest));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        return destinationRepository.findById(id).map(dest -> {
            destinationRepository.delete(dest);
            return ResponseEntity.noContent().<Void>build();
        }).orElse(ResponseEntity.notFound().build());
    }
}