package com.haefliger.cryptomonitor.validation;

import com.haefliger.cryptomonitor.validation.validator.TipoIndicadorValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Constraint(validatedBy = TipoIndicadorValidator.class)
@Target({ FIELD })
@Retention(RUNTIME)
public @interface TipoIndicadorValido {
    String message() default "Tipo de indicador inválido. Valores válidos: PRECO, RSI";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

