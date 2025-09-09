package ru.yandex.practicum.filmorate.storage.genre;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;

@JdbcTest
@AutoConfigureTestDatabase
@Import(GenreDbStorage.class)
class GenreDbStorageTest {

    @Autowired
    private GenreDbStorage storage;

    @Test
    void getAll_ok() {
        List<Genre> all = storage.getAll();
        Assertions.assertThat(all).isNotEmpty();
        Assertions.assertThat(all.get(0).getId()).isEqualTo(1);
    }

    @Test
    void getById_ok() {
        Genre g = storage.getById(1).orElseThrow();
        Assertions.assertThat(g.getName()).isNotBlank();
    }
}