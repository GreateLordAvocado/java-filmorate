package ru.yandex.practicum.filmorate.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.service.GenreService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/genres")
@RequiredArgsConstructor
public class GenreController {
    private final GenreService genreService;


    @GetMapping
    public ResponseEntity<List<Genre>> getAll() {
        log.info("Запрос всех жанров");
        return ResponseEntity.ok(genreService.getAll());
    }


    @GetMapping("/{id}")
    public ResponseEntity<Genre> getById(@PathVariable int id) {
        log.info("Запрос жанра id={}", id);
        return ResponseEntity.ok(genreService.getById(id));
    }
}