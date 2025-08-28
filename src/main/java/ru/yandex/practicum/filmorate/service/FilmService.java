package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.ConflictException;
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

    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public Film create(Film film) {
        log.debug("Создание фильма: {}", film);

        boolean duplicateExists = filmStorage.getAll().stream()
                .anyMatch(f -> f.getName().equalsIgnoreCase(film.getName())
                        && Objects.equals(f.getReleaseDate(), film.getReleaseDate()));
        if (duplicateExists) {
            log.warn("Попытка создать дубликат фильма: {}", film);
            throw new ConflictException("Фильм с таким названием и датой релиза уже существует");
        }

        if (film.getMpa() != null && film.getMpa().getId() != null) {
            Mpa mpa = film.getMpa();
            int id = mpa.getId();
            if (id < 1 || id > 5) {
                throw new NotFoundException("MPA с id=" + id + " не найден");
            }
            film.setMpa(new Mpa(id, mpa.getName()));
        }

        film.setGenres(normalizeGenres(film.getGenres()));

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

        if (film.getMpa() != null && film.getMpa().getId() != null) {
            Mpa mpa = film.getMpa();
            int id = mpa.getId();
            if (id < 1 || id > 5) {
                throw new NotFoundException("MPA с id=" + id + " не найден");
            }
            film.setMpa(new Mpa(id, mpa.getName()));
        }

        film.setGenres(normalizeGenres(film.getGenres()));

        return filmStorage.update(film);
    }

    public Film getById(Long id) {
        log.debug("Поиск фильма по id={}", id);
        Film f = filmStorage.getById(id)
                .orElseThrow(() -> {
                    log.warn("Фильм с id={} не найден", id);
                    return new NotFoundException("Фильм с id=" + id + " не найден");
                });
        f.setGenres(normalizeGenres(f.getGenres()));
        return f;
    }

    public List<Film> getAll() {
        log.debug("Запрос на получение всех фильмов");
        List<Film> list = filmStorage.getAll();
        list.forEach(f -> f.setGenres(normalizeGenres(f.getGenres())));
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
                .peek(f -> f.setGenres(normalizeGenres(f.getGenres())))
                .sorted(Comparator.comparingInt((Film f) -> f.getLikes().size()).reversed())
                .limit(count)
                .collect(Collectors.toList());
    }

    private Set<Genre> normalizeGenres(Set<Genre> input) {
        if (input == null || input.isEmpty()) return new LinkedHashSet<>();
        return input.stream()
                .filter(Objects::nonNull)
                .map(Genre::getId)
                .filter(Objects::nonNull)
                .peek(id -> {
                    if (id < 1 || id > 6) {
                        throw new NotFoundException("Жанр с id=" + id + " не найден");
                    }
                })
                .distinct()
                .sorted()
                .map(id -> new Genre(id, null))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}