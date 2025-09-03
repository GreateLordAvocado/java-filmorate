package ru.yandex.practicum.filmorate.storage.film;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import({FilmDbStorage.class, UserDbStorage.class})
class FilmDbStorageTest {

    @Autowired
    private FilmDbStorage filmStorage;

    @Autowired
    private UserDbStorage userStorage;

    private Film makeFilm() {
        Film f = new Film();
        f.setName("Test film");
        f.setDescription("Desc");
        f.setReleaseDate(LocalDate.of(2000, 1, 1));
        f.setDuration(100);
        f.setMpa(new Mpa(1, "G"));
        Set<Genre> gs = new LinkedHashSet<>();
        gs.add(new Genre(1, null));
        gs.add(new Genre(2, null));
        f.setGenres(gs);
        return f;
    }

    private User makeUser(String login, String email) {
        User u = new User();
        u.setLogin(login);
        u.setEmail(email);
        u.setName("");
        u.setBirthday(LocalDate.of(1990, 1, 1));
        return u;
    }

    @Test
    void createAndLoadWithGenresAndMpa() {
        Film saved = filmStorage.create(makeFilm());

        Optional<Film> found = filmStorage.getById(saved.getId());
        assertThat(found).isPresent();

        Film f = found.get();
        assertThat(f.getName()).isEqualTo("Test film");
        assertThat(f.getMpa().getId()).isEqualTo(1);
        assertThat(f.getMpa().getName()).isEqualTo("G");

        assertThat(f.getGenres()).extracting(Genre::getId)
                .containsExactly(1, 2);
    }

    @Test
    void updateAlsoUpdatesGenresAndLikes() {
        Film saved = filmStorage.create(makeFilm());

        User u1 = userStorage.create(makeUser("u1", "u1@example.com"));
        User u2 = userStorage.create(makeUser("u2", "u2@example.com"));

        Set<Genre> gs = new LinkedHashSet<>();
        gs.add(new Genre(2, null));
        gs.add(new Genre(3, null));
        saved.setGenres(gs);

        saved.getLikes().clear();
        saved.getLikes().add(u1.getId());
        saved.getLikes().add(u2.getId());

        Film updated = filmStorage.update(saved);

        Film reloaded = filmStorage.getById(updated.getId()).orElseThrow();
        assertThat(reloaded.getGenres()).extracting(Genre::getId)
                .containsExactly(2, 3);
        assertThat(reloaded.getLikes()).containsExactlyInAnyOrder(u1.getId(), u2.getId());
    }

    @Test
    void getAllReturnsSaved() {
        Film f1 = filmStorage.create(makeFilm());

        Film f2 = makeFilm();
        f2.setName("Another test film");
        f2.setReleaseDate(LocalDate.of(2001, 1, 1));
        f2 = filmStorage.create(f2);

        assertThat(filmStorage.getAll())
                .extracting(Film::getId)
                .contains(f1.getId(), f2.getId());
    }
}