package ru.yandex.practicum.filmorate.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:filmorate-${random.uuid};MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.driverClassName=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=password",
        "spring.sql.init.mode=always"
})
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class UserControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testlogin");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));
    }

    @Test
    @DisplayName("POST /users — успешное создание пользователя")
    void shouldCreateUser() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    @DisplayName("POST /users — ошибка при пустом email")
    void shouldFailWhenEmailIsEmpty() throws Exception {
        user.setEmail("");
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /users — ошибка при некорректном email")
    void shouldFailWhenEmailIsInvalid() throws Exception {
        user.setEmail("invalid-email");
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /users — ошибка при пустом логине")
    void shouldFailWhenLoginIsEmpty() throws Exception {
        user.setLogin("");
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /users — ошибка при логине с пробелами")
    void shouldFailWhenLoginContainsSpaces() throws Exception {
        user.setLogin("invalid login");
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /users — пустое имя заменяется на логин")
    void shouldSetLoginAsNameWhenNameIsEmpty() throws Exception {
        user.setName("");
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("testlogin"));
    }

    @Test
    @DisplayName("POST /users — ошибка при дате рождения в будущем")
    void shouldFailWhenBirthdayInFuture() throws Exception {
        user.setBirthday(LocalDate.now().plusDays(1));
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /users — получение списка пользователей")
    void shouldGetAllUsers() throws Exception {
        mockMvc.perform(get("/users"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /users — успешное обновление пользователя")
    void shouldUpdateUser() throws Exception {
        String createdUserJson = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andReturn().getResponse().getContentAsString();

        User createdUser = objectMapper.readValue(createdUserJson, User.class);

        createdUser.setName("Updated Name");
        createdUser.setEmail("updated_" + createdUser.getId() + "@example.com");
        createdUser.setLogin("updatedLogin" + createdUser.getId());
        createdUser.setBirthday(LocalDate.of(1990, 1, 1));

        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createdUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"))
                .andExpect(jsonPath("$.email").value("updated_" + createdUser.getId() + "@example.com"))
                .andExpect(jsonPath("$.login").value("updatedLogin" + createdUser.getId()));
    }

    @Test
    @DisplayName("PUT /users — ошибка при обновлении несуществующего пользователя")
    void shouldFailUpdateNonExistingUser() throws Exception {
        user.setId(999L);
        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /users/{id} — успешное получение пользователя по id")
    void shouldGetUserById() throws Exception {
        String createdUserJson = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andReturn().getResponse().getContentAsString();

        User createdUser = objectMapper.readValue(createdUserJson, User.class);

        mockMvc.perform(get("/users/{id}", createdUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdUser.getId()));
    }

    @Test
    @DisplayName("GET /users/{id} — ошибка при запросе несуществующего пользователя")
    void shouldFailWhenUserNotFoundById() throws Exception {
        mockMvc.perform(get("/users/{id}", 999))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /users/{id} — успешное удаление пользователя")
    void shouldDeleteUser() throws Exception {
        String createdUserJson = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andReturn().getResponse().getContentAsString();

        User createdUser = objectMapper.readValue(createdUserJson, User.class);

        mockMvc.perform(delete("/users/{id}", createdUser.getId()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /users/{id} — ошибка при удалении несуществующего пользователя")
    void shouldFailWhenDeleteNonExistingUser() throws Exception {
        mockMvc.perform(delete("/users/{id}", 999))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /users/{id}/friends/{friendId} — успешное добавление в друзья")
    void shouldAddFriend() throws Exception {
        String user1Json = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andReturn().getResponse().getContentAsString();
        User user1 = objectMapper.readValue(user1Json, User.class);

        User friend = new User();
        friend.setEmail("friend@example.com");
        friend.setLogin("friendlogin");
        friend.setName("Friend User");
        friend.setBirthday(LocalDate.of(1995, 5, 5));

        String user2Json = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(friend)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andReturn().getResponse().getContentAsString();
        User user2 = objectMapper.readValue(user2Json, User.class);

        mockMvc.perform(put("/users/{id}/friends/{friendId}", user1.getId(), user2.getId()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /users/{id}/friends/{friendId} — успешное удаление из друзей")
    void shouldRemoveFriend() throws Exception {
        User user1 = new User();
        user1.setEmail("user1@example.com");
        user1.setLogin("user1login");
        user1.setName("User One");
        user1.setBirthday(LocalDate.of(1990, 1, 1));

        String user1Json = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andReturn().getResponse().getContentAsString();
        User createdUser1 = objectMapper.readValue(user1Json, User.class);

        User user2 = new User();
        user2.setEmail("user2@example.com");
        user2.setLogin("user2login");
        user2.setName("User Two");
        user2.setBirthday(LocalDate.of(1992, 2, 2));

        String user2Json = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andReturn().getResponse().getContentAsString();
        User createdUser2 = objectMapper.readValue(user2Json, User.class);

        mockMvc.perform(put("/users/{id}/friends/{friendId}", createdUser1.getId(), createdUser2.getId()))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/users/{id}/friends/{friendId}", createdUser1.getId(), createdUser2.getId()))
                .andExpect(status().isOk());
    }
}