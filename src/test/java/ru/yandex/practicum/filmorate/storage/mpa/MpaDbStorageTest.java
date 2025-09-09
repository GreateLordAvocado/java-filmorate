package ru.yandex.practicum.filmorate.storage.mpa;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;

@JdbcTest
@AutoConfigureTestDatabase
@Import(MpaDbStorage.class)
class MpaDbStorageTest {

    @Autowired
    private MpaDbStorage storage;

    @Test
    void getAll_ok() {
        List<Mpa> all = storage.getAll();
        Assertions.assertThat(all).hasSizeGreaterThanOrEqualTo(5);
        Assertions.assertThat(all.get(0).getId()).isEqualTo(1);
    }

    @Test
    void getById_ok() {
        Mpa mpa = storage.getById(1).orElseThrow();
        Assertions.assertThat(mpa.getName()).isNotBlank();
    }
}