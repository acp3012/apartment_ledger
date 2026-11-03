package com.yesh.apartmentledger.core.validation;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValidMonthTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void acceptsMonthsWithinCalendarRange() {
        assertTrue(validator.validate(new MonthRequest((short) 1)).isEmpty());
        assertTrue(validator.validate(new MonthRequest((short) 12)).isEmpty());
    }

    @Test
    void rejectsMonthsOutsideCalendarRange() {
        assertFalse(validator.validate(new MonthRequest((short) 0)).isEmpty());
        assertFalse(validator.validate(new MonthRequest((short) 13)).isEmpty());
    }

    @Test
    void allowsNullForOptionalMonthInputs() {
        assertTrue(validator.validate(new MonthRequest(null)).isEmpty());
    }

    private record MonthRequest(@ValidMonth Short month) {
    }
}