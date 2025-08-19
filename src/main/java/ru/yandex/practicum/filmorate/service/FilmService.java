package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ConflictException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Comparator;
import java.util.List;
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
                        && f.getReleaseDate().equals(film.getReleaseDate()));

        if (duplicateExists) {
            log.warn("Попытка создать дубликат фильма: {}", film);
            throw new ConflictException("Фильм с таким названием и датой релиза уже существует");
        }

        return filmStorage.create(film);
    }

    public Film update(Film film) {
        log.debug("Обновление фильма: {}", film);

        if (filmStorage.getById(film.getId()).isEmpty()) {
            log.warn("Фильм с id={} не найден", film.getId());
            throw new NotFoundException("Фильм с id=" + film.getId() + " не найден");
        }

        return filmStorage.update(film);
    }

    public Film getById(Long id) {
        log.debug("Поиск фильма по id={}", id);
        return filmStorage.getById(id)
                .orElseThrow(() -> {
                    log.warn("Фильм с id={} не найден", id);
                    return new NotFoundException("Фильм с id=" + id + " не найден");
                });
    }

    public List<Film> getAll() {
        log.debug("Запрос на получение всех фильмов");
        return filmStorage.getAll();
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
        List<Film> popular = filmStorage.getAll().stream()
                .sorted(Comparator.comparingInt((Film f) -> f.getLikes().size()).reversed())
                .limit(count)
                .collect(Collectors.toList());
        log.debug("Найдено популярных фильмов: {}", popular.size());
        return popular;
    }

}