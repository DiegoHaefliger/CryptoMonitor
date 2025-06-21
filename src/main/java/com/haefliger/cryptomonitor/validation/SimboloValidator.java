package com.haefliger.cryptomonitor.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class SimboloValidator implements ConstraintValidator<SimboloValido, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return false;
        }
        // Normaliza o valor removendo espaços e hífens
        String sanitized = value.replace("-", "").replaceAll("\\s", "");
        return !sanitized.isEmpty() && sanitized.equals(value);
    }
}
