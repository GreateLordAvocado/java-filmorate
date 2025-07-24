package ru.yandex.practicum.filmorate.controllers;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exceptions.*;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static ru.yandex.practicum.filmorate.utils.ControllersUtils.getNextId;
import static ru.yandex.practicum.filmorate.utils.FilmValidate.validateFilm;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {

    private final Map<Long, Film> films = new HashMap<>();

    @GetMapping
    public Collection<Film> findAll() {
        log.info("Получен запрос на вывод списка всех фильмов");
        return films.values();
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film newFilm) {
        log.info("Получен запрос на добавление фильма {}", newFilm.getName());

        if (newFilm == null) {
            log.error("Попытка добавить null");
            throw new NullPointerException("Фильм не может быть null");
        }

        validateFilm(newFilm);
        newFilm.setId(getNextId(films.keySet()));
        films.put(newFilm.getId(), newFilm);

        log.info("Фильм {} успешно добавлен", newFilm.getName());
        return newFilm;
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film film) {
        log.info("Получен запрос на обновление фильма c id {}", film.getId());

        if (film.getId() == null) {
            log.error("Попытка обновить фильм с id = null");
            throw new ValidationException("Id фильма не может быть null");
        }

        if (!films.containsKey(film.getId())) {
            log.error("Попытка обновить фильм, не найденный по id:{}", film.getId());
            throw new NotFoundException("Фильм с id:" + film.getId() + " не найден");
        }

        validateFilm(film);
        films.put(film.getId(), film);
        log.info("Фильм {} c id:{} успешно обновлен", film.getName(), film.getId());
        return film;
    }
}