package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    private static final Map<Integer, String> MPA_NAMES = Map.of(
            1, "G",
            2, "PG",
            3, "PG-13",
            4, "R",
            5, "NC-17"
    );

    private static final Map<Integer, String> GENRE_NAMES = Map.of(
            1, "Комедия",
            2, "Драма",
            3, "Мультфильм",
            4, "Триллер",
            5, "Документальный",
            6, "Боевик"
    );

    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public Film create(Film film) {
        log.debug("Создание фильма: {}", film);
        normalizeMpa(film);
        normalizeGenres(film);
        return filmStorage.create(film);
    }

    public Film update(Film film) {
        log.debug("Обновление фильма: {}", film);
        if (film.getId() == null) {
            log.warn("Попытка обновить фильм без id (null)");
            throw new NotFoundException("Фильм с id=null не найден");
        }
        if (filmStorage.getById(film.getId()).isEmpty()) {
            log.warn("Фильм с id={} не найден", film.getId());
            throw new NotFoundException("Фильм с id=" + film.getId() + " не найден");
        }
        normalizeMpa(film);
        normalizeGenres(film);
        return filmStorage.update(film);
    }

    public Film getById(Long id) {
        log.debug("Поиск фильма по id={}", id);
        Film f = filmStorage.getById(id)
                .orElseThrow(() -> {
                    log.warn("Фильм с id={} не найден", id);
                    return new NotFoundException("Фильм с id=" + id + " не найден");
                });
        normalizeMpa(f);
        normalizeGenres(f);
        return f;
    }

    public List<Film> getAll() {
        log.debug("Запрос на получение всех фильмов");
        List<Film> list = filmStorage.getAll();
        list.forEach(f -> {
            normalizeMpa(f);
            normalizeGenres(f);
        });
        return list;
    }

    public void delete(Long id) {
        log.info("Удаление фильма id={}", id);
        filmStorage.delete(id);
    }

    public boolean addLike(Long filmId, Long userId) {
        log.info("Добавление лайка фильму id={} от пользователя id={}", filmId, userId);
        Film film = getById(filmId);
        userStorage.getById(userId)
                .orElseThrow(() -> {
                    log.warn("Пользователь с id={} не найден", userId);
                    return new NotFoundException("Пользователь с id=" + userId + " не найден");
                });
        boolean added = film.getLikes().add(userId);
        log.debug("Лайк {}: фильм id={}, всего лайков={}",
                added ? "добавлен" : "уже был", filmId, film.getLikes().size());
        return added;
    }

    public void removeLike(Long filmId, Long userId) {
        log.info("Удаление лайка у фильма id={} от пользователя id={}", filmId, userId);
        Film film = getById(filmId);
        userStorage.getById(userId)
                .orElseThrow(() -> {
                    log.warn("Пользователь с id={} не найден", userId);
                    return new NotFoundException("Пользователь с id=" + userId + " не найден");
                });
        film.getLikes().remove(userId);
        log.debug("Лайк удалён: фильм id={}, всего лайков={}", filmId, film.getLikes().size());
    }

    public List<Film> getPopular(int count) {
        log.info("Получение списка популярных фильмов (топ-{})", count);
        return filmStorage.getAll().stream()
                .peek(f -> {
                    normalizeMpa(f);
                    normalizeGenres(f);
                })
                .sorted(Comparator.comparingInt((Film f) -> f.getLikes().size()).reversed())
                .limit(count)
                .collect(Collectors.toList());
    }

    private void normalizeMpa(Film film) {
        if (film.getMpa() == null || film.getMpa().getId() == null) {
            return;
        }
        Integer id = film.getMpa().getId();
        String name = MPA_NAMES.get(id);
        if (name == null) {
            throw new NotFoundException("MPA с id=" + id + " не найден");
        }
        film.setMpa(new Mpa(id, name));
    }

    private void normalizeGenres(Film film) {
        Set<Genre> input = film.getGenres();
        if (input == null || input.isEmpty()) {
            film.setGenres(new LinkedHashSet<>());
            return;
        }
        LinkedHashSet<Genre> normalized = input.stream()
                .filter(Objects::nonNull)
                .map(Genre::getId)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .map(id -> {
                    String name = GENRE_NAMES.get(id);
                    if (name == null) {
                        throw new NotFoundException("Жанр с id=" + id + " не найден");
                    }
                    return new Genre(id, name);
                })
                .collect(Collectors.toCollection(LinkedHashSet::new));
        film.setGenres(normalized);
    }
}