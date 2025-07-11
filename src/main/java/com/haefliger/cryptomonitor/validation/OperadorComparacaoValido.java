package com.haefliger.cryptomonitor.validation;

import com.haefliger.cryptomonitor.validation.validator.OperadorComparacaoValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Constraint(validatedBy = OperadorComparacaoValidator.class)
@Target({ FIELD })
@Retention(RUNTIME)
public @interface OperadorComparacaoValido {
    String message() default "Operador inválido. Valores válidos: <, >, =";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

