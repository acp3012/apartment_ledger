package com.yesh.apartmentledger.core.flat.controller;


import com.yesh.apartmentledger.core.flat.dto.FlatResponse;
import com.yesh.apartmentledger.core.flat.repository.FlatRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/apartments/{apartmentId}")
public class FlatController {

    private final FlatRepository flatRepository;

    public FlatController(FlatRepository flatRepository) {
        this.flatRepository = flatRepository;
    }

    @GetMapping("/flats")
    public ResponseEntity<List<FlatResponse>> getFlatsByApartment(@PathVariable Long apartmentId) {

        List<FlatResponse> flats = flatRepository.findByApartmentId(apartmentId)
                .stream()
                .map(flat -> new FlatResponse(flat.getId(), flat.getFlatNumber(), flat.getOwnerName())) // Map entity to DTO
                .collect(Collectors.toList());

        return ResponseEntity.ok(flats);
    }
}