package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;

@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> films = new HashMap<>();
    private long idCounter = 0;

    @Override
    public Film create(Film film) {
        film.setId(++idCounter);
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Film update(Film film) {
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Optional<Film> getById(Long id) {
        return Optional.ofNullable(films.get(id));
    }

    @Override
    public List<Film> getAll() {
        return new ArrayList<>(films.values());
    }

    @Override
    public void delete(Long id) {
        films.remove(id);
    }

    // Работа с лайками через методы Film
    public void addLike(Long filmId, Long userId) {
        films.get(filmId).addLike(userId);
    }

    public void removeLike(Long filmId, Long userId) {
        films.get(filmId).removeLike(userId);
    }

    public int getLikesCount(Long filmId) {
        Film film = films.get(filmId);
        return (film != null) ? film.getLikesCount() : 0;
    }
}