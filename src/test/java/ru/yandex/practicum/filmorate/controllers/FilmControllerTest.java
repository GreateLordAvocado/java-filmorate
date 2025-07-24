package ru.yandex.practicum.filmorate.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exceptions.*;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class FilmControllerTest {

    private FilmController filmController;

    @BeforeEach
    public void beforeEach() {
        filmController = new FilmController();
    }

    // Тест успешного создания фильма с валидными данными
    @Test
    public void create_allRequiredFieldsValid_filmAddedWithGeneratedId() {
        Film film = new Film();
        film.setName("Фильм 1");
        film.setDescription("Описание фильма");
        film.setReleaseDate(LocalDate.of(2003, 7, 19));
        film.setDuration(120);

        Film createdFilm = filmController.create(film);

        assertNotNull(createdFilm.getId(), "Фильму не был присвоен id");
        assertEquals(1, filmController.findAll().size(),
                "Неверное количество фильмов после создания");
    }

    // Тест граничного условия для даты релиза - релиз раньше 28.12.1895 не допустим
    @Test
    public void create_releaseDateBeforeFirstFilm_throwsValidationException() {
        Film film = new Film();
        film.setName("Фильм с некорректной датой релиза");
        film.setDescription("Описание фильма");
        film.setReleaseDate(LocalDate.of(1759, 10, 21));
        film.setDuration(120);

        assertThrows(ValidationException.class, () -> filmController.create(film),
                "Ожидалось ValidationException при дате релиза раньше 28.12.1895");
    }

    // Тест граничного условия для даты релиза - релиз 28.12.1895 должен быть допустим
    @Test
    public void create_releaseDateEqualsFirstFilm_noValidationErrors() {
        Film film = new Film();
        film.setName("Старый фильм");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(1895, 12, 28));
        film.setDuration(15);

        assertDoesNotThrow(() -> filmController.create(film), "Дата релиза 28.12.1895 должна быть допустима");
    }

    // Тест добавления фильма без даты релиза - релиз не может быть null
    @Test
    public void create_releaseDateNull_throwsValidationException() {
        Film film = new Film();
        film.setName("Фильм");
        film.setDescription("Описание");
        film.setReleaseDate(null);
        film.setDuration(15);

        assertThrows(NullPointerException.class, () -> filmController.create(film),
                "Дата релиза не может быть null");
    }

    // Тест обновления несуществующего фильма
    @Test
    public void update_nonExistentFilmId_throwsNotFoundException() {
        Film film = new Film();
        film.setId(14L);
        film.setName("Фильм");
        film.setDescription("Описание фильма");
        film.setReleaseDate(LocalDate.of(1995, 12, 28));
        film.setDuration(120);

        assertThrows(NotFoundException.class, () -> filmController.update(film),
                "Ожидалось NotFoundException при обновлении несуществующего фильма");
    }

    // Тест проверяет валидацию Id при обновлении - Id не должен быть null
    @Test
    public void update_nullId_throwsValidationException() {
        Film film = new Film();
        film.setId(null);
        film.setName("Фильма с нулевым id");
        film.setDescription("Описание фильма");
        film.setReleaseDate(LocalDate.of(2025, 1, 14));
        film.setDuration(120);

        assertThrows(ValidationException.class, () -> filmController.update(film),
                "Ожидалось ValidationException при обновлении с null ID");
    }

    // Тест на успешное обновление фильма
    @Test
    public void update_existingFilmWithValidData_fieldsUpdated() {
        // Создание фильма
        Film film = new Film();
        film.setName("Время ведьм");
        film.setDescription("Не все души можно спасти");
        film.setReleaseDate(LocalDate.of(2010, 1, 6));
        film.setDuration(98);
        Film createdFilm = filmController.create(film);

        // Обновление фильма
        Film updatedFilm = new Film();
        updatedFilm.setId(createdFilm.getId());
        updatedFilm.setName("Уже не Начало");
        updatedFilm.setDescription("Просто описание");
        updatedFilm.setReleaseDate(LocalDate.of(1996, 1, 1));
        updatedFilm.setDuration(150);
        Film result = filmController.update(updatedFilm);

        assertEquals("Уже не Начало", result.getName(), "Название фильма не обновилось");
        assertEquals("Просто описание", result.getDescription(), "Описание фильма не обновилось");
        assertEquals(150, result.getDuration(), "Продолжительность фильма не обновилась");
    }

    // Тест получение списка всех созданных фильмов
    @Test
    public void findAll_afterAddingTwoFilms_returnsCollectionSize2() {
        // Создаем 2 фильма
        Film film1 = new Film();
        film1.setName(" Властелин колец: Братство Кольца");
        film1.setDescription("Описание фильма");
        film1.setReleaseDate(LocalDate.of(2001, 12, 10));
        film1.setDuration(228);
        filmController.create(film1);

        Film film2 = new Film();
        film2.setName(" Властелин колец: Две крепости");
        film2.setDescription("Описание продолжения");
        film2.setReleaseDate(LocalDate.of(2002, 12, 18));
        film2.setDuration(235);
        filmController.create(film2);

        assertEquals(2, filmController.findAll().size(), "Неверное количество фильмов в списке");
    }

    // Тест ограничений условия для описания - 200 символов должно быть допустимо
    @Test
    public void create_descriptionExactly200Chars_noValidationErrors() {
        Film film = new Film();
        film.setName("Фильм");
        film.setDescription("A".repeat(200));
        film.setReleaseDate(LocalDate.of(2025, 7, 22));
        film.setDuration(95);

        assertDoesNotThrow(() -> filmController.create(film), "Описание в 200 символов должно быть допустимо");
    }

    // Тест ограничений условия описания - длина описания не может превышать 200 символов
    @Test
    public void create_description201Chars_throwsValidationException() {
        Film film = new Film();
        film.setName("Фильм");
        film.setDescription("А".repeat(201));
        film.setReleaseDate(LocalDate.of(1995, 12, 28));
        film.setDuration(120);

        assertThrows(ValidationException.class, () -> filmController.create(film),
                "Ожидалось ValidationException при описании длиной 201 символ ");
    }

    // Тест ограничений условия продолжительности: минимальная продолжительность (1 минута) должна быть допустима
    @Test
    public void create_durationOne_noValidationErrors() {
        Film film = new Film();
        film.setName("Фильм");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(2025, 5, 30));
        film.setDuration(1);

        assertDoesNotThrow(() -> filmController.create(film),
                "Продолжительность фильма в 1 минуту должна быть допустима");
    }

    // Тест ограничений условия продолжительности - продолжительность должна быть больше 0
    @Test
    public void create_duration0_throwsValidationException() {
        Film film = new Film();
        film.setName("Фильм");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(1982, 12, 28));
        film.setDuration(0);

        assertThrows(ValidationException.class, () -> filmController.create(film),
                "Ожидалось ValidationException при продолжительности 0");
    }

    // Тест ограничений условия продолжительности - продолжительность не может быть меньше 0
    @Test
    public void create_durationNegative_throwsValidationException() {
        Film film = new Film();
        film.setName("Фильм");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(1982, 12, 28));
        film.setDuration(-1);

        assertThrows(ValidationException.class, () -> filmController.create(film),
                "Ожидалось ValidationException при отрицательной продолжительности");
    }

    // Тест проверяет валидацию имени фильма - пустая строка недопустима
    @Test
    public void create_EmptyName_throwsValidationException() {
        Film film = new Film();
        film.setName("");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(2024, 12, 31));
        film.setDuration(160);

        assertThrows(ValidationException.class, () -> filmController.create(film),
                "Ожидалось ValidationException при пустом имени фильма");
    }

    // Тест проверяет невозможность добавить фильм равный null
    @Test
    public void create_nullFilm_throwsValidationException() {
        assertThrows(NullPointerException.class,
                () -> filmController.create(null),
                "Ожидалось NullPointerException, если объект Film = null");
    }
}