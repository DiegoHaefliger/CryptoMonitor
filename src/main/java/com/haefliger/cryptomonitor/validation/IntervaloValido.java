package com.haefliger.cryptomonitor.validation;

import com.haefliger.cryptomonitor.validation.validator.IntervaloValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Constraint(validatedBy = IntervaloValidator.class)
@Target({ FIELD })
@Retention(RUNTIME)
public @interface IntervaloValido {
    String message() default "Intervalo inválido.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
