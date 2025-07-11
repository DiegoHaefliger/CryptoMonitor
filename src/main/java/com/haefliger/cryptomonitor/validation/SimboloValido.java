package com.haefliger.cryptomonitor.validation;

import com.haefliger.cryptomonitor.validation.validator.SimboloValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Constraint(validatedBy = SimboloValidator.class)
@Target({ FIELD })
@Retention(RUNTIME)
public @interface SimboloValido {
    String message() default "Símbolo inválido: não pode ser vazio, não pode conter espaços ou hífens";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

