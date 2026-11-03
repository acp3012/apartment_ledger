package com.yesh.apartmentledger.core.flat.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.yesh.apartmentledger.core.apartment.entity.Apartment;
import com.yesh.apartmentledger.core.apartment.repository.ApartmentRepository;
import com.yesh.apartmentledger.core.flat.dto.FlatRequest;
import com.yesh.apartmentledger.core.flat.entity.Flat;
import com.yesh.apartmentledger.core.flat.repository.FlatRepository;
import com.yesh.apartmentledger.exception.ResourceAlreadyExistsException;

class FlatServiceTest {

    @Test
    void createFlat_whenFlatNumberAlreadyExistsForApartment_throwsException() {
        Long apartmentId = 10L;
        Apartment apartment = new Apartment();
        apartment.setId(apartmentId);

        FlatRequest request = new FlatRequest(
                "A-101",
                (short) 5,
                "Alice",
                "9876543210",
                "alice@example.com",
                BigDecimal.valueOf(1200)
        );

        FlatRepository flatRepository = repositoryWithDuplicateFlatNumber(apartmentId, request.flatNumber());
        ApartmentRepository apartmentRepository = repositoryWithApartment(apartment);
        FlatService flatService = new FlatService(flatRepository, apartmentRepository);

        assertThrows(ResourceAlreadyExistsException.class, () -> flatService.createFlat(apartmentId, request));
    }

    @Test
    void createFlat_whenOwnerMobileAlreadyExistsForApartment_throwsException() {
        Long apartmentId = 10L;
        Apartment apartment = new Apartment();
        apartment.setId(apartmentId);

        FlatRequest request = new FlatRequest(
                "A-102",
                (short) 6,
                "Bob",
                "9876543211",
                "bob@example.com",
                BigDecimal.valueOf(1300)
        );

        FlatRepository flatRepository = repositoryWithDuplicateOwnerMobile(apartmentId, request.ownerMobileNumber());
        ApartmentRepository apartmentRepository = repositoryWithApartment(apartment);
        FlatService flatService = new FlatService(flatRepository, apartmentRepository);

        assertThrows(ResourceAlreadyExistsException.class, () -> flatService.createFlat(apartmentId, request));
    }

    @Test
    void createFlat_whenRequestIsValid_savesFlatAndReturnsResponse() {
        Long apartmentId = 10L;
        Apartment apartment = new Apartment();
        apartment.setId(apartmentId);

        FlatRequest request = new FlatRequest(
                "A-103",
                (short) 7,
                "Charlie",
                "9876543212",
                "charlie@example.com",
                BigDecimal.valueOf(1400)
        );

        List<Flat> saved = new ArrayList<>();
        FlatRepository flatRepository = (FlatRepository) Proxy.newProxyInstance(
                FlatRepository.class.getClassLoader(),
                new Class<?>[]{FlatRepository.class},
                (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "findByFlatNumberAndApartmentId":
                            return Optional.empty();
                        case "findByOwnerMobileAndApartmentId":
                            return Optional.empty();
                        case "save":
                            saved.add((Flat) args[0]);
                            return args[0];
                        default:
                            return null;
                    }
                }
        );

        ApartmentRepository apartmentRepository = repositoryWithApartment(apartment);
        FlatService flatService = new FlatService(flatRepository, apartmentRepository);

        var response = flatService.createFlat(apartmentId, request);

        assertEquals(apartmentId, response.apartmentId());
        assertEquals("A-103", response.flatNumber());
        assertEquals("Charlie", response.ownerName());
        assertEquals(1, saved.size());
    }

    @Test
    void updateFlat_whenSetupIsComplete_updatesExistingFlatWithoutChangingItsId() {
        Long apartmentId = 10L;
        Long flatId = 25L;
        Apartment apartment = new Apartment();
        apartment.setId(apartmentId);
        apartment.setIsSetupCompleted(true);

        Flat existingFlat = new Flat();
        existingFlat.setId(flatId);
        existingFlat.setApartment(apartment);
        existingFlat.setFlatNumber("A-101");
        existingFlat.setFloorNumber((short) 5);
        existingFlat.setOwnerName("Alice");
        existingFlat.setOwnerMobile("9876543210");
        existingFlat.setOwnerEmail("alice@example.com");

        FlatRequest request = new FlatRequest(
                "A-101",
                (short) 5,
                "Alice Updated",
                "9876543210",
                "alice.updated@example.com",
                BigDecimal.valueOf(1200)
        );
        FlatRepository flatRepository = (FlatRepository) Proxy.newProxyInstance(
                FlatRepository.class.getClassLoader(),
                new Class<?>[]{FlatRepository.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "findByIdAndApartmentId",
                    "findByFlatNumberAndApartmentId",
                    "findByOwnerMobileAndApartmentId" -> Optional.of(existingFlat);
                    case "save" -> args[0];
                    default -> null;
                }
        );
        FlatService flatService = new FlatService(
                flatRepository,
                repositoryWithApartment(apartment)
        );

        var response = flatService.updateFlat(apartmentId, flatId, request);

        assertEquals(flatId, response.flatId());
        assertEquals("Alice Updated", response.ownerName());
        assertEquals("alice.updated@example.com", response.ownerEmail());
    }

    private ApartmentRepository repositoryWithApartment(Apartment apartment) {
        return (ApartmentRepository) Proxy.newProxyInstance(
                ApartmentRepository.class.getClassLoader(),
                new Class<?>[]{ApartmentRepository.class},
                (proxy, method, args) -> {
                    if ("findById".equals(method.getName())) {
                        return Optional.ofNullable(apartment).filter(entity -> entity.getId().equals(args[0]));
                    }
                    return null;
                }
        );
    }

    private FlatRepository repositoryWithDuplicateFlatNumber(Long apartmentId, String flatNumber) {
        return (FlatRepository) Proxy.newProxyInstance(
                FlatRepository.class.getClassLoader(),
                new Class<?>[]{FlatRepository.class},
                (proxy, method, args) -> {
                    if ("findByFlatNumberAndApartmentId".equals(method.getName())) {
                        if (args[0].equals(flatNumber) && args[1].equals(apartmentId)) {
                            return Optional.of(new Flat());
                        }
                        return Optional.empty();
                    }
                    return null;
                }
        );
    }

    private FlatRepository repositoryWithDuplicateOwnerMobile(Long apartmentId, String ownerMobile) {
        return (FlatRepository) Proxy.newProxyInstance(
                FlatRepository.class.getClassLoader(),
                new Class<?>[]{FlatRepository.class},
                (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "findByFlatNumberAndApartmentId":
                            return Optional.empty();
                        case "findByOwnerMobileAndApartmentId":
                            if (args[0].equals(ownerMobile) && args[1].equals(apartmentId)) {
                                return Optional.of(new Flat());
                            }
                            return Optional.empty();
                        default:
                            return null;
                    }
                }
        );
    }
}
