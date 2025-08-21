package ru.yandex.practicum.filmorate.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
public class FilmController {

    private final FilmService filmService;

    @GetMapping
    public ResponseEntity<Collection<Film>> findAll() {
        log.info("Запрос списка всех фильмов");
        return ResponseEntity.ok(filmService.getAll());
    }

    @PostMapping
    public ResponseEntity<Film> create(@Valid @RequestBody Film newFilm) {
        log.info("Запрос на добавление фильма: {}", newFilm.getName());
        Film created = filmService.create(newFilm);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping
    public ResponseEntity<Film> update(@Valid @RequestBody Film film) {
        log.info("Запрос на обновление фильма id={}", film.getId());
        Film updated = filmService.update(film);
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/{id}/like/{userId}")
    public ResponseEntity<Void> addLike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Добавление лайка фильму id={} от пользователя id={}", id, userId);
        filmService.addLike(id, userId);
        return ResponseEntity.noContent().build(); // 204
    }

    @DeleteMapping("/{id}/like/{userId}")
    public ResponseEntity<Void> removeLike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Удаление лайка у фильма id={} от пользователя id={}", id, userId);
        filmService.removeLike(id, userId);
        return ResponseEntity.noContent().build(); // 204
    }

    @GetMapping("/popular")
    public ResponseEntity<Collection<Film>> getPopular(@RequestParam(defaultValue = "10") int count) {
        log.info("Запрос популярных фильмов, limit={}", count);
        return ResponseEntity.ok(filmService.getPopular(count));
    }
}