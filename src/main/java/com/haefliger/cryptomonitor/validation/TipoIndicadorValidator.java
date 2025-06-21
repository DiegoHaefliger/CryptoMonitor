package com.haefliger.cryptomonitor.validation;

import com.haefliger.cryptomonitor.enums.TipoIndicadorEnum;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class TipoIndicadorValidator implements ConstraintValidator<TipoIndicadorValido, TipoIndicadorEnum> {
    @Override
    public boolean isValid(TipoIndicadorEnum value, ConstraintValidatorContext context) {
        if (value == null || !TipoIndicadorEnum.isValid(value.name())) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    "Tipo de indicador inválido. Valores válidos: " + TipoIndicadorEnum.valoresValidos()
            ).addConstraintViolation();
            return false;
        }
        return true;
    }
}

