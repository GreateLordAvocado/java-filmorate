package ru.yandex.practicum.filmorate.controllers;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.IdGenerator;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {

    private final Map<Long, Film> films = new HashMap<>();
    private final IdGenerator idGenerator = new IdGenerator();

    // Создаем валидатор локально, чтобы он был всегда доступен и не был null
    private final Validator validator;

    public FilmController() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        this.validator = factory.getValidator();
    }

    @GetMapping
    public Collection<Film> findAll() {
        log.info("Получен запрос на вывод списка всех фильмов");
        return films.values();
    }

    @PostMapping
    public Film create(@RequestBody Film newFilm) {
        validateFilm(newFilm);

        newFilm.setId(idGenerator.getNextId(films.keySet()));
        films.put(newFilm.getId(), newFilm);

        log.info("Фильм {} успешно добавлен", newFilm.getName());
        return newFilm;
    }

    @PutMapping
    public Film update(@RequestBody Film film) {
        validateFilm(film);

        if (film.getId() == null) {
            log.error("Попытка обновить фильм с id = null");
            throw new ValidationException("Id фильма не может быть null");
        }

        if (!films.containsKey(film.getId())) {
            log.error("Попытка обновить фильм, не найденный по id:{}", film.getId());
            throw new NotFoundException("Фильм с id:" + film.getId() + " не найден");
        }

        films.put(film.getId(), film);
        log.info("Фильм {} c id:{} успешно обновлен", film.getName(), film.getId());
        return film;
    }

    private void validateFilm(Film film) {
        if (film == null) {
            throw new ValidationException("Фильм не может быть null");
        }

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        if (!violations.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (ConstraintViolation<Film> violation : violations) {
                sb.append(violation.getMessage()).append("; ");
            }
            throw new ValidationException(sb.toString());
        }
    }
}