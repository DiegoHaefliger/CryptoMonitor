package com.haefliger.cryptomonitor.validation;

import com.haefliger.cryptomonitor.enums.OperadorComparacaoEnum;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class OperadorComparacaoValidator implements ConstraintValidator<OperadorComparacaoValido, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.trim().isEmpty() || !OperadorComparacaoEnum.isValid(value)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    "Operador inválido. Valores válidos: " + OperadorComparacaoEnum.valoresValidos()
            ).addConstraintViolation();
            return false;
        }
        return true;
    }

}

