package com.haefliger.cryptomonitor.validation.validator;

import com.haefliger.cryptomonitor.enums.IntervaloEnum;
import com.haefliger.cryptomonitor.validation.IntervaloValido;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class IntervaloValidator implements ConstraintValidator<IntervaloValido, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || !IntervaloEnum.isValid(value)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                "Intervalo inválido. Valores válidos: " + IntervaloEnum.valoresValidos()
            ).addConstraintViolation();
            return false;
        }
        return true;
    }
}
