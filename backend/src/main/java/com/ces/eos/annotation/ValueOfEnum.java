package com.ces.eos.annotation;

import com.ces.eos.validator.ValueOfEnumValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Constraint(validatedBy = ValueOfEnumValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValueOfEnum {
  Class<? extends Enum<?>> enumClass();

  String message() default "must be any of enum {acceptedValues}";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
