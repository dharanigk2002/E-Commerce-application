package com.ecommerce.common.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = ValidImageUrlValidator.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidImageUrl {
    public String message() default "Image url must start with /uploads/image";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
