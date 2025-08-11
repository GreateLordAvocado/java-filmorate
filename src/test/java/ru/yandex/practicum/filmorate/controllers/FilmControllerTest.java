package ru.yandex.practicum.filmorate.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.model.Film;

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
        validFilm = new Film();
        validFilm.setName("Интерстеллар");
        validFilm.setDescription("Следующий шаг человечества станет величайшим");
        validFilm.setReleaseDate(LocalDate.of(2014, 11, 7));
        validFilm.setDuration(169);
    }

    @Test
    void create_validFilm_returns200() throws Exception {
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validFilm)))
                .andExpect(status().isOk())
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
    void update_nullId_returns400() throws Exception {
        validFilm.setId(null);

        mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validFilm)))
                .andExpect(status().isBadRequest());
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
}