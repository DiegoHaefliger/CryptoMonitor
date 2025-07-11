package com.haefliger.cryptomonitor.validation.validator;

import com.haefliger.cryptomonitor.enums.OperadorComparacaoEnum;
import com.haefliger.cryptomonitor.validation.OperadorComparacaoValido;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class OperadorComparacaoValidator implements ConstraintValidator<OperadorComparacaoValido, OperadorComparacaoEnum> {
    @Override
    public boolean isValid(OperadorComparacaoEnum value, ConstraintValidatorContext context) {
        if (value == null ||  !OperadorComparacaoEnum.isValid(value)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    "Operador inválido. Valores válidos: " + OperadorComparacaoEnum.valoresValidos()
            ).addConstraintViolation();
            return false;
        }
        return true;
    }

}

