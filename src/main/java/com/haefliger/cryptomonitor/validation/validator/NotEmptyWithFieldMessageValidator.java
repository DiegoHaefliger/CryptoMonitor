package com.haefliger.cryptomonitor.validation.validator;

import com.haefliger.cryptomonitor.validation.NotEmptyWithFieldMessage;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class NotEmptyWithFieldMessageValidator implements ConstraintValidator<NotEmptyWithFieldMessage, Object> {
    private String fieldName;

    @Override
    public void initialize(NotEmptyWithFieldMessage constraintAnnotation) {
        this.fieldName = constraintAnnotation.fieldName();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null || (value instanceof String && ((String) value).trim().isEmpty())) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Campo '" + fieldName + "' não pode ser vazio")
                    .addConstraintViolation();
            return false;
        }
        return true;
    }
}
