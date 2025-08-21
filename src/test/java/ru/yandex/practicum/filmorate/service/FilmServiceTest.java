package ru.yandex.practicum.filmorate.service;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FilmServiceTest {

    private FilmStorage filmStorage;
    private UserStorage userStorage;
    private FilmService filmService;

    @BeforeEach
    void setUp() {
        filmStorage = mock(FilmStorage.class);
        userStorage = mock(UserStorage.class);
        filmService = new FilmService(filmStorage, userStorage);
    }

    private Film createFilm(Long id, String name) {
        Film film = new Film();
        film.setId(id);
        film.setName(name);
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        return film;
    }

    @Test
    void shouldAddLikeToFilm() {
        Film film = createFilm(1L, "Film 1");

        when(filmStorage.getById(1L)).thenReturn(Optional.of(film));
        when(userStorage.getById(2L)).thenReturn(Optional.of(new User()));

        filmService.addLike(1L, 2L);

        assertTrue(film.getLikes().contains(2L));
        assertEquals(1, film.getLikes().size());
    }

    @Test
    void shouldRemoveLikeFromFilm() {
        Film film = createFilm(1L, "Film 1");
        film.getLikes().add(2L);

        when(filmStorage.getById(1L)).thenReturn(Optional.of(film));
        when(userStorage.getById(2L)).thenReturn(Optional.of(new User()));

        filmService.removeLike(1L, 2L);

        assertFalse(film.getLikes().contains(2L));
        assertEquals(0, film.getLikes().size());
    }

    @Test
    void shouldReturnPopularFilmsByLikes() {
        Film film1 = createFilm(1L, "Film 1");
        Film film2 = createFilm(2L, "Film 2");

        film1.getLikes().addAll(Set.of(1L, 2L));
        film2.getLikes().add(1L);

        when(filmStorage.getAll()).thenReturn(List.of(film1, film2));

        List<Film> popular = filmService.getPopular(1);

        assertEquals(1, popular.size());
        assertEquals(film1, popular.get(0));
    }
}