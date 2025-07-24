package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * Film.
 */
@Data
@EqualsAndHashCode(of = {"id"})
public class Film {

    private Long id;

    @NotBlank(message = "Название фильма не может быть пустым")
    private String name;

    @Size(max = 200, message = "Длина описания не может превышать 200 символов")
    private String description;

    @NotNull(message = "Дата релиза не может быть пустой")
    private LocalDate releaseDate;

    @Positive(message = "Продолжительность фильма должна быть положительной")
    private Integer duration;
}
