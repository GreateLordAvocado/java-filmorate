package ru.yandex.practicum.filmorate.utils;

import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;

/**
 * Утилитарный класс для валидации объектов типа Film
 */

@Slf4j
public class FilmValidate {
    public static final LocalDate DATE_FIRST_FILM = LocalDate.of(1895, 12, 28);
    public static final int MAX_DESCRIPTION_LENGTH = 200;

    // Метод проверки выполнения необходимых условий
    public static void validateFilm(Film film) {

        if (film.getName() == null || film.getName().isEmpty()) {
            log.error("Попытка добавить фильм без названия");
            throw new ValidationException("Название фильма не должно быть пустым");
        }

        if (film.getReleaseDate().isBefore(DATE_FIRST_FILM)) {
            log.error("Попытка добавить фильм с недопустимой датой выхода");
            throw new ValidationException("Недопустимая дата выхода");
        }

        if (film.getDescription() != null && film.getDescription().length() > MAX_DESCRIPTION_LENGTH) {
            log.error("Попытка добавить фильм с недопустимой длиной описания");
            throw new ValidationException("Недопустимая длина описания");
        }

        if (film.getDuration() <= 0) {
            log.error("Попытка добавить фильм с некорректной продолжительностью");
            throw new ValidationException("Недопустимая продолжительность фильма");
        }
    }
}