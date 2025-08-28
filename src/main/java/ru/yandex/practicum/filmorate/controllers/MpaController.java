package ru.yandex.practicum.filmorate.controllers;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.service.MpaService;


import java.util.List;


@Slf4j
@RestController
@RequestMapping("/mpa")
@RequiredArgsConstructor
public class MpaController {
    private final MpaService mpaService;


    @GetMapping
    public ResponseEntity<List<Mpa>> getAll() {
        log.info("Запрос всех MPA");
        return ResponseEntity.ok(mpaService.getAll());
    }


    @GetMapping("/{id}")
    public ResponseEntity<Mpa> getById(@PathVariable int id) {
        log.info("Запрос MPA id={}", id);
        return ResponseEntity.ok(mpaService.getById(id));
    }
}