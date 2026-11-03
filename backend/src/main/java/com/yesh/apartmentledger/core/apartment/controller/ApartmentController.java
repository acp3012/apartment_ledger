package com.yesh.apartmentledger.core.apartment.controller;

import com.yesh.apartmentledger.core.apartment.dto.ApartmentActiveStatusRequest;
import com.yesh.apartmentledger.core.apartment.dto.ApartmentCreateRequest;
import com.yesh.apartmentledger.core.apartment.dto.ApartmentResponse;
import com.yesh.apartmentledger.core.apartment.dto.GoLivePeriodRequest;
import com.yesh.apartmentledger.core.apartment.service.ApartmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/apartments")
@Tag(name = "Apartment management", description = "Create apartments and manage their active status.")
public class ApartmentController {

    private final ApartmentService apartmentService;

    @Operation (summary = "Get all apartments",
            description = "Returns a list of all apartments, including their details and active status.")
    @GetMapping()
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ApartmentResponse>> getAllApartments() {
        return ResponseEntity.ok(apartmentService.getAllApartments());
    }
        
    @GetMapping("/{apartmentId}")
    @PreAuthorize("@apartmentAuthorization.canAccess(#p0)")
    @Operation(summary = "Get an apartment by ID",
            description = "Returns the details of a specific apartment based on its ID.")
    public ResponseEntity<ApartmentResponse> getApartmentById(
            @Parameter(description = "ID of the apartment.") @PathVariable Long apartmentId) {
        return ResponseEntity.ok(apartmentService.getApartmentById(apartmentId));
    }

    // create apartment
    @Operation(summary = "Create an apartment",
            description = "Creates an apartment with the supplied details. New apartments are active by default.")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PostMapping
    public ResponseEntity<ApartmentResponse> createApartment(
            @Valid @RequestBody ApartmentCreateRequest request) {
        ApartmentResponse response = apartmentService.createApartment(request);
        URI location = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{apartmentId}")
            .buildAndExpand(response.apartmentId())
            .toUri();
        return ResponseEntity.created(location).body(response);
    }
    // update active status of apartment
    @Operation(summary = "Enable or disable an apartment",
            description = "Updates only the apartment's active status; other apartment details remain unchanged.")
    
    @PreAuthorize("hasRole('ADMIN') and @apartmentAuthorization.canAccess(#p0)")
    @PutMapping("/{apartmentId}")
    public ResponseEntity<ApartmentResponse> updateActiveStatus(
            @Parameter(description = "ID of the apartment.") @PathVariable Long apartmentId,
            @Valid @RequestBody ApartmentActiveStatusRequest request) {
        return ResponseEntity.ok(apartmentService.updateActiveStatus(apartmentId, request));
    }

    @Operation(summary = "Configure the apartment's financial go-live period",
            description = "Updates the month in which financial transactions begin. This can only be changed before transactions are recorded.")
    @PreAuthorize("hasRole('ADMIN') and @apartmentAuthorization.canAccess(#p0)")
    @PatchMapping("/{apartmentId}/go-live-period")
    public ResponseEntity<ApartmentResponse> updateGoLivePeriod(
            @Parameter(description = "ID of the apartment.") @PathVariable Long apartmentId,
            @Valid @RequestBody GoLivePeriodRequest request) {
        return ResponseEntity.ok(apartmentService.updateGoLivePeriod(apartmentId, request));
    }

    @Operation(summary = "Mark the apartment's one-time setup as completed",
            description = "Completes setup after at least one flat and the financial starting period are configured. Opening balance is optional.")
    @PreAuthorize("hasRole('ADMIN') and @apartmentAuthorization.canAccess(#p0)")
    @PatchMapping("/{apartmentId}/setup-completed")
    public ResponseEntity<ApartmentResponse> completeInitialSetup(
            @Parameter(description = "ID of the apartment.") @PathVariable Long apartmentId) {
        return ResponseEntity.ok(apartmentService.completeInitialSetup(apartmentId));
    }
}