package ru.yandex.practicum.filmorate.controllers;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {

    private final FilmService filmService;

    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @GetMapping
    public ResponseEntity<Collection<Film>> findAll() {
        log.info("Получен запрос на вывод списка всех фильмов");
        return ResponseEntity.ok(filmService.getAll());
    }

    @PostMapping
    public ResponseEntity<Film> create(@Valid @RequestBody Film newFilm) {
        log.info("Получен запрос на добавление фильма {}",
                newFilm.getName() != null ? newFilm.getName() : "<null>");
        Film created = filmService.create(newFilm); // если дубликат → выбросит ConflictException
        log.debug("Фильм сохранён: {}", created);
        return ResponseEntity.ok(created);
    }

    @PutMapping
    public ResponseEntity<Film> update(@Valid @RequestBody Film film) {
        log.info("Получен запрос на обновление фильма id={}", film.getId());

        if (film.getId() == null) {
            log.warn("Попытка обновления фильма без id");
            return ResponseEntity.badRequest().build();
        }

        try {
            Film updated = filmService.update(film);
            return ResponseEntity.ok(updated);
        } catch (NotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/like/{userId}")
    public ResponseEntity<Void> addLike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Получен запрос на добавление лайка фильму id={} от пользователя id={}", id, userId);
        filmService.addLike(id, userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/like/{userId}")
    public ResponseEntity<Void> removeLike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Получен запрос на удаление лайка у фильма id={} от пользователя id={}", id, userId);
        filmService.removeLike(id, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/popular")
    public ResponseEntity<Collection<Film>> getPopular(@RequestParam(defaultValue = "10") int count) {
        log.info("Получен запрос на список популярных фильмов, limit={}", count);
        return ResponseEntity.ok(filmService.getPopular(count));
    }
}