package com.haefliger.cryptomonitor.validation;

import com.haefliger.cryptomonitor.validation.validator.OperadorLogicoValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Constraint(validatedBy = OperadorLogicoValidator.class)
@Target({ FIELD })
@Retention(RUNTIME)
public @interface OperadorLogicoValido {
    String message() default "Operador lógico inválido. Valores válidos: AND, OR";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

