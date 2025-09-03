package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final GenreService genreService;
    private final MpaService mpaService;

    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage,
                       @Qualifier("userDbStorage") UserStorage userStorage,
                       GenreService genreService,
                       MpaService mpaService) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.genreService = genreService;
        this.mpaService = mpaService;
    }

    public Film create(Film film) {
        log.debug("Создание фильма: {}", film);
        canonicalizeMpaAndGenres(film);
        return filmStorage.create(film);
    }

    public Film update(Film film) {
        log.debug("Обновление фильма: {}", film);
        if (film.getId() == null) {
            log.warn("Попытка обновить фильм без id (null)");
            throw new NotFoundException("Фильм с id=null не найден");
        }
        filmStorage.getById(film.getId()).orElseThrow(() -> {
            log.warn("Фильм с id={} не найден", film.getId());
            return new NotFoundException("Фильм с id=" + film.getId() + " не найден");
        });

        canonicalizeMpaAndGenres(film);
        return filmStorage.update(film);
    }

    public Film getById(Long id) {
        log.debug("Поиск фильма по id={}", id);
        return filmStorage.getById(id).orElseThrow(() -> {
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
        userStorage.getById(userId).orElseThrow(() -> {
            log.warn("Пользователь с id={} не найден", userId);
            return new NotFoundException("Пользователь с id=" + userId + " не найден");
        });

        boolean added = film.getLikes().add(userId);
        filmStorage.update(film);
        log.debug("Лайк {}: фильм id={}, всего лайков={}",
                added ? "добавлен" : "уже был", filmId, film.getLikes().size());
        return added;
    }

    public void removeLike(Long filmId, Long userId) {
        log.info("Удаление лайка у фильма id={} от пользователя id={}", filmId, userId);
        Film film = getById(filmId);
        userStorage.getById(userId).orElseThrow(() -> {
            log.warn("Пользователь с id={} не найден", userId);
            return new NotFoundException("Пользователь с id=" + userId + " не найден");
        });

        film.getLikes().remove(userId);
        filmStorage.update(film);
        log.debug("Лайк удалён: фильм id={}, всего лайков={}", filmId, film.getLikes().size());
    }

    public List<Film> getPopular(int count) {
        log.info("Получение списка популярных фильмов (топ-{})", count);
        return filmStorage.getAll().stream()
                .sorted(Comparator.comparingInt((Film f) -> f.getLikes().size()).reversed())
                .limit(count)
                .collect(Collectors.toList());
    }

    private void canonicalizeMpaAndGenres(Film film) {
        if (film.getMpa() == null || film.getMpa().getId() == null) {
            throw new NotFoundException("MPA с id=null не найден");
        }
        Mpa canonicalMpa = mpaService.getById(film.getMpa().getId());
        film.setMpa(canonicalMpa);

        Set<Genre> input = film.getGenres();
        if (input == null || input.isEmpty()) {
            film.setGenres(new LinkedHashSet<>());
            return;
        }
        Set<Genre> normalized = input.stream()
                .filter(Objects::nonNull)
                .map(Genre::getId)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .map(genreService::getById)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        film.setGenres(normalized);
    }
}