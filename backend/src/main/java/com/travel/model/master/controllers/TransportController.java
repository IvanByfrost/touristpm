package com.travel.model.master.controllers;

import com.travel.model.master.Transport;
import com.travel.model.master.repository.TransportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/transports")
@RequiredArgsConstructor
public class TransportController {

    private final TransportRepository transportRepository;

    @GetMapping
    public List<Transport> getAll() {
        return transportRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Transport> getById(@PathVariable UUID id) {
        return transportRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Transport create(@RequestBody Transport transport) {
        return transportRepository.save(transport);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Transport> update(@PathVariable UUID id, @RequestBody Transport details) {
        return transportRepository.findById(id).map(trans -> {
            trans.setTransportType(details.getTransportType());
            trans.setProviderCompany(details.getProviderCompany());
            trans.setMaxCapacity(details.getMaxCapacity());
            return ResponseEntity.ok(transportRepository.save(trans));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        return transportRepository.findById(id).map(trans -> {
            transportRepository.delete(trans);
            return ResponseEntity.noContent().<Void>build();
        }).orElse(ResponseEntity.notFound().build());
    }
}