package com.tcheepeng.tracket.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@NotBlank(message = "Currency does not exist")
@Size(max = 4, message = "Currency cannot be more than 4 characters")
@Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {})
public @interface ValidCurrency {
  String message() default "Currency is invalid";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
