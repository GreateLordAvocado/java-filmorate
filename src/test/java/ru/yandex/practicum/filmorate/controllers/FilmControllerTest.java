package ru.yandex.practicum.filmorate.controllers;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.ConstraintViolation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exceptions.*;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

class FilmControllerTest {

    private FilmController filmController;

    @BeforeEach
    public void beforeEach() {
        filmController = new FilmController();
    }

    @Test
    public void create_allRequiredFieldsValid_filmAddedWithGeneratedId() {
        Film film = new Film();
        film.setName("Фильм 1");
        film.setDescription("Описание фильма");
        film.setReleaseDate(LocalDate.of(2003, 7, 19));
        film.setDuration(120);

        validate(film);
        Film createdFilm = filmController.create(film);

        assertThat(createdFilm.getId()).as("Фильму не был присвоен id").isNotNull();
        assertThat(filmController.findAll()).hasSize(1);
    }

    @Test
    public void create_releaseDateBeforeFirstFilm_throwsValidationException() {
        Film film = new Film();
        film.setName("Фильм с некорректной датой релиза");
        film.setDescription("Описание фильма");
        film.setReleaseDate(LocalDate.of(1759, 10, 21));
        film.setDuration(120);

        // убран вызов validate(film);
        assertThatThrownBy(() -> filmController.create(film))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Дата релиза");
    }

    @Test
    public void create_releaseDateEqualsFirstFilm_noValidationErrors() {
        Film film = new Film();
        film.setName("Старый фильм");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(1895, 12, 28));
        film.setDuration(15);

        validate(film);
        assertThatCode(() -> filmController.create(film)).doesNotThrowAnyException();
    }

    @Test
    public void create_releaseDateNull_throwsValidationException() {
        Film film = new Film();
        film.setName("Фильм");
        film.setDescription("Описание");
        film.setReleaseDate(null);
        film.setDuration(15);

        assertThatThrownBy(() -> validate(film))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    public void update_nonExistentFilmId_throwsNotFoundException() {
        Film film = new Film();
        film.setId(14L);
        film.setName("Фильм");
        film.setDescription("Описание фильма");
        film.setReleaseDate(LocalDate.of(1995, 12, 28));
        film.setDuration(120);

        validate(film);
        assertThatThrownBy(() -> filmController.update(film))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("не найден");
    }

    @Test
    public void update_nullId_throwsValidationException() {
        Film film = new Film();
        film.setId(null);
        film.setName("Фильма с нулевым id");
        film.setDescription("Описание фильма");
        film.setReleaseDate(LocalDate.of(2025, 1, 14));
        film.setDuration(120);

        validate(film);
        assertThatThrownBy(() -> filmController.update(film))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Id фильма не может быть null");
    }

    @Test
    public void update_existingFilmWithValidData_fieldsUpdated() {
        Film film = new Film();
        film.setName("Время ведьм");
        film.setDescription("Не все души можно спасти");
        film.setReleaseDate(LocalDate.of(2010, 1, 6));
        film.setDuration(98);
        validate(film);
        Film createdFilm = filmController.create(film);

        Film updatedFilm = new Film();
        updatedFilm.setId(createdFilm.getId());
        updatedFilm.setName("Уже не Начало");
        updatedFilm.setDescription("Просто описание");
        updatedFilm.setReleaseDate(LocalDate.of(1996, 1, 1));
        updatedFilm.setDuration(150);
        validate(updatedFilm);
        Film result = filmController.update(updatedFilm);

        assertThat(result.getName()).as("Название фильма не обновилось").isEqualTo("Уже не Начало");
        assertThat(result.getDescription()).as("Описание фильма не обновилось").isEqualTo("Просто описание");
        assertThat(result.getDuration()).as("Продолжительность фильма не обновилась").isEqualTo(150);
    }

    @Test
    public void findAll_afterAddingTwoFilms_returnsCollectionSize2() {
        Film film1 = new Film();
        film1.setName(" Властелин колец: Братство Кольца");
        film1.setDescription("Описание фильма");
        film1.setReleaseDate(LocalDate.of(2001, 12, 10));
        film1.setDuration(228);
        validate(film1);
        filmController.create(film1);

        Film film2 = new Film();
        film2.setName(" Властелин колец: Две крепости");
        film2.setDescription("Описание продолжения");
        film2.setReleaseDate(LocalDate.of(2002, 12, 18));
        film2.setDuration(235);
        validate(film2);
        filmController.create(film2);

        assertThat(filmController.findAll()).hasSize(2);
    }

    @Test
    public void create_descriptionExactly200Chars_noValidationErrors() {
        Film film = new Film();
        film.setName("Фильм");
        film.setDescription("A".repeat(200));
        film.setReleaseDate(LocalDate.of(2025, 7, 22));
        film.setDuration(95);

        validate(film);
        assertThatCode(() -> filmController.create(film)).doesNotThrowAnyException();
    }

    @Test
    public void create_description201Chars_throwsValidationException() {
        Film film = new Film();
        film.setName("Фильм");
        film.setDescription("А".repeat(201));
        film.setReleaseDate(LocalDate.of(1995, 12, 28));
        film.setDuration(120);

        assertThatThrownBy(() -> validate(film))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    public void create_durationOne_noValidationErrors() {
        Film film = new Film();
        film.setName("Фильм");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(2025, 5, 30));
        film.setDuration(1);

        validate(film);
        assertThatCode(() -> filmController.create(film)).doesNotThrowAnyException();
    }

    @Test
    public void create_duration0_throwsValidationException() {
        Film film = new Film();
        film.setName("Фильм");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(1982, 12, 28));
        film.setDuration(0);

        assertThatThrownBy(() -> validate(film))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    public void create_durationNegative_throwsValidationException() {
        Film film = new Film();
        film.setName("Фильм");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(1982, 12, 28));
        film.setDuration(-1);

        assertThatThrownBy(() -> validate(film))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    public void create_EmptyName_throwsValidationException() {
        Film film = new Film();
        film.setName("");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(2024, 12, 31));
        film.setDuration(160);

        assertThatThrownBy(() -> validate(film))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    public void create_nullFilm_throwsValidationException() {
        assertThatThrownBy(() -> filmController.create(null))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Фильм не может быть null");
    }

    private void validate(Film film) {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        if (!violations.isEmpty()) {
            throw new ValidationException("Ошибки валидации: " + violations);
        }
    }
}