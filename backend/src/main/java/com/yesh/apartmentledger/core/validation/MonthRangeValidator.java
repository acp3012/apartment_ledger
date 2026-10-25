package com.yesh.apartmentledger.core.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class MonthRangeValidator implements ConstraintValidator<ValidMonth, Short> {

    @Override
    public boolean isValid(Short month, ConstraintValidatorContext context) {
        return isValidMonth(month);
    }

    public static boolean isValidMonth(Short month) {
        return month == null || (month >= 1 && month <= 12);
    }
}