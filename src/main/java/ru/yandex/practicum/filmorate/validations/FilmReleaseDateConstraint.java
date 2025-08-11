package ru.yandex.practicum.filmorate.validations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = FilmReleaseDateValidator.class)
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface FilmReleaseDateConstraint {
    String message() default "Дата релиза не может быть раньше 28.12.1895";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}