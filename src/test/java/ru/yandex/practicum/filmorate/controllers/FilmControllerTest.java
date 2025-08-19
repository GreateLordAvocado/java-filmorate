package ru.yandex.practicum.filmorate.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class FilmControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private Film validFilm;

    @BeforeEach
    void setup() {
        validFilm = createFilm(
                "Интерстеллар",
                "Следующий шаг человечества станет величайшим",
                LocalDate.of(2014, 11, 7),
                169
        );
    }

    private Film createFilm(String name, String description, LocalDate releaseDate, int duration) {
        Film film = new Film();
        film.setName(name);
        film.setDescription(description);
        film.setReleaseDate(releaseDate);
        film.setDuration(duration);
        return film;
    }

    private User createUser(String email, String login, String name, LocalDate birthday) {
        User user = new User();
        user.setEmail(email);
        user.setLogin(login);
        user.setName(name);
        user.setBirthday(birthday);
        return user;
    }

    @Test
    void create_validFilm_returns201() throws Exception {
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validFilm)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    void create_releaseDateTooEarly_returns400() throws Exception {
        validFilm.setReleaseDate(LocalDate.of(1700, 1, 1));

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validFilm)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_nullReleaseDate_returns400() throws Exception {
        validFilm.setReleaseDate(null);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validFilm)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_nonExistentId_returns404() throws Exception {
        validFilm.setId(999L);

        mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validFilm)))
                .andExpect(status().isNotFound());
    }

    @Test
    void update_nullId_returns404() throws Exception {
        validFilm.setId(null);

        mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validFilm)))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_description201Chars_returns400() throws Exception {
        validFilm.setDescription("А".repeat(201));

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validFilm)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_durationZero_returns400() throws Exception {
        validFilm.setDuration(0);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validFilm)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_blankName_returns400() throws Exception {
        validFilm.setName("  ");

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validFilm)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_nullFilm_returns400() throws Exception {
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("null"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Должен вернуть 201 при создании нового фильма и 409 при дубликате")
    void shouldReturnCreatedOrConflictForDuplicateFilm() throws Exception {
        Film film1 = createFilm("Film 1", "Description", LocalDate.of(2000, 1, 1), 120);
        Film film2 = createFilm("Film 1", "Another description", LocalDate.of(2000, 1, 1), 130);

        // Добавление первого фильма → 201 Created
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film1)))
                .andExpect(status().isCreated());

        // Добавление второго фильма (дубликата) → 409 Conflict
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film2)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Должен вернуть 400 при null в названии фильма")
    void shouldReturnBadRequestWhenFilmNameIsNull() throws Exception {
        Film film = createFilm(null, "Description", LocalDate.of(2000, 1, 1), 120);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void shouldReturnBadRequestWhenFilmNameIsBlank() throws Exception {
        Film film = createFilm("", "Описание", LocalDate.of(2000, 1, 1), 100);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenDescriptionTooLong() throws Exception {
        Film film = createFilm("Film", "A".repeat(201), LocalDate.of(2000, 1, 1), 100);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenDurationIsNegative() throws Exception {
        Film film = createFilm("Film", "Описание", LocalDate.of(2000, 1, 1), -10);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldNotAllowSameUserToLikeFilmTwice() throws Exception {
        User user = createUser("user@mail.com", "user", "User Name", LocalDate.of(1990, 1, 1));
        Film film = createFilm("Film Title", "Description", LocalDate.of(2000, 1, 1), 120);

        String userResponse = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk()) // 200 OK при создании пользователя
                .andReturn().getResponse().getContentAsString();
        Long userId = objectMapper.readValue(userResponse, User.class).getId();

        String filmResponse = mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isCreated()) // 201 Created при создании фильма
                .andReturn().getResponse().getContentAsString();
        Long filmId = objectMapper.readValue(filmResponse, Film.class).getId();

        // Первый лайк → 201 Created
        mockMvc.perform(put("/films/" + filmId + "/like/" + userId))
                .andExpect(status().isCreated());

        // Второй лайк от того же пользователя → 200 OK
        mockMvc.perform(put("/films/" + filmId + "/like/" + userId))
                .andExpect(status().isOk());

        // Проверка, что всего 1 лайк
        mockMvc.perform(get("/films/popular"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(filmId))
                .andExpect(jsonPath("$[0].likes.length()").value(1));
    }
}