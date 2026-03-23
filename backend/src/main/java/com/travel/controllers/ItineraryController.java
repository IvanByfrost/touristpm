package com.travel.controllers;

import com.travel.model.booking.Itinerary;
import com.travel.repository.ItineraryRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/itineraries")
@RequiredArgsConstructor
public class ItineraryController {

    private final ItineraryRepository itineraryRepository;

    @GetMapping
    public List<Itinerary> getAll() {
        return itineraryRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Itinerary> getById(@PathVariable UUID id) {
        return itineraryRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Itinerary create(@RequestBody Itinerary itinerary) {
        return itineraryRepository.save(itinerary);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Itinerary> update(@PathVariable UUID id, @RequestBody Itinerary details) {
        return itineraryRepository.findById(id).map(i -> {
            i.setDestination(details.getDestination());
            i.setStartDate(details.getStartDate());
            i.setEndDate(details.getEndDate());
            i.setActivities(details.getActivities());
            return ResponseEntity.ok(itineraryRepository.save(i));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        return itineraryRepository.findById(id).map(i -> {
            itineraryRepository.delete(i);
            return ResponseEntity.noContent().<Void>build();
        }).orElse(ResponseEntity.notFound().build());
    }
}