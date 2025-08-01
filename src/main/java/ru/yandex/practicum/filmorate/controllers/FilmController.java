package ru.yandex.practicum.filmorate.controllers;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import jakarta.validation.ValidationException;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.IdGenerator;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {

    private final Map<Long, Film> films = new HashMap<>();
    private final IdGenerator idGenerator = new IdGenerator();

    @GetMapping
    public Collection<Film> findAll() {
        log.info("Получен запрос на вывод списка всех фильмов");
        return films.values();
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film newFilm) {
        if (newFilm == null) {
            throw new ValidationException("Фильм не может быть null");
        }

        newFilm.setId(idGenerator.getNextId(films.keySet()));
        films.put(newFilm.getId(), newFilm);
        return newFilm;
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film film) {
        if (film == null) {
            throw new ValidationException("Фильм не может быть null");
        }

        if (film.getId() == null) {
            throw new ValidationException("Id фильма не может быть null");
        }

        if (!films.containsKey(film.getId())) {
            throw new NotFoundException("Фильм с id:" + film.getId() + " не найден");
        }

        films.put(film.getId(), film);
        return film;
    }
}