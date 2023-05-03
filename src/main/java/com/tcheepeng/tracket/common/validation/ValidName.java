package com.tcheepeng.tracket.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@NotBlank(message = "Account name cannot be empty")
@Size(max = 255, message = "Name cannot be more than 255 characters")
@Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {})
public @interface ValidName {
  String message() default "Name is invalid";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
