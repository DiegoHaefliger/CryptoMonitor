package com.haefliger.cryptomonitor.validation;

import com.haefliger.cryptomonitor.enums.OperadorLogicoEnum;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class OperadorLogicoValidator implements ConstraintValidator<OperadorLogicoValido, OperadorLogicoEnum> {
    @Override
    public boolean isValid(OperadorLogicoEnum value, ConstraintValidatorContext context) {
        if (value == null || !OperadorLogicoEnum.isValid(value.name())) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                "Operador lógico inválido. Valores válidos: " + OperadorLogicoEnum.valoresValidos()
            ).addConstraintViolation();
            return false;
        }
        return true;
    }
}
