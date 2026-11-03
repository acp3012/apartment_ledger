package com.yesh.apartmentledger.core.flat.dto;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

class FlatRequestValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void acceptsShortFloorNumber() {
        FlatRequest request = new FlatRequest(
                "A-101",
                (short) 5,
                "Alice",
                "9876543210",
                "alice@example.com",
                BigDecimal.valueOf(1200)
        );

        assertTrue(validator.validate(request).isEmpty());
    }

    @Test
    void rejectsOwnerMobileNumberContainingLetters() {
        FlatRequest request = new FlatRequest(
                "A-101",
                (short) 5,
                "Alice",
                "98765abc10",
                "alice@example.com",
                BigDecimal.valueOf(1200)
        );

        assertFalse(validator.validate(request).isEmpty());
    }

    @Test
    void rejectsOwnerMobileNumberThatIsNotExactlyTenDigits() {
        FlatRequest request = new FlatRequest(
                "A-101",
                (short) 5,
                "Alice",
                "987654321",
                "alice@example.com",
                BigDecimal.valueOf(1200)
        );

        assertFalse(validator.validate(request).isEmpty());
    }
}
