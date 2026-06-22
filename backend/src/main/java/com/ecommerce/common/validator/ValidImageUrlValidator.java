package com.ecommerce.common.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidImageUrlValidator implements ConstraintValidator<ValidImageUrl, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if(value==null || value.isBlank())
            return true;
        return value.startsWith("/uploads/image");
    }
}
