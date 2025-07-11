package com.haefliger.cryptomonitor.validation;

import com.haefliger.cryptomonitor.validation.validator.NotEmptyWithFieldMessageValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = NotEmptyWithFieldMessageValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface NotEmptyWithFieldMessage {
    String message() default "";

    String fieldName();

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
