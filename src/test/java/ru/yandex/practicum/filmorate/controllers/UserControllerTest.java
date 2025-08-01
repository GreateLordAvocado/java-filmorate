package ru.yandex.practicum.filmorate.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD) // аннотация для для сброса перед тестами
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String toJson(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }

    @Test
    public void create_allRequiredFieldsValid_userAddedWithGeneratedId() throws Exception {
        User user = new User(null, "user@mail.ru", "user_login", null, LocalDate.of(1995, 2, 13));
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    public void create_emptyName_nameEqualsLogin() throws Exception {
        User user = new User(null, "user@mail.ru", "user_login", "", LocalDate.of(1995, 2, 13));
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("user_login"));
    }

    @Test
    public void create_invalidEmailFormat_throwsValidationException() throws Exception {
        User user = new User(null, "invalid-email", "login", "name", LocalDate.of(1995, 2, 13));
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(user)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void create_nullEmail_throwsValidationException() throws Exception {
        User user = new User(null, null, "login", "name", LocalDate.of(1995, 2, 13));
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(user)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void create_duplicateEmail_throwsDuplicateException() throws Exception {
        User user = new User(null, "duplicate@mail.ru", "login1", "Name1", LocalDate.of(1995, 1, 1));
        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(user))).andExpect(status().isOk());

        User duplicate = new User(null, "duplicate@mail.ru", "login2", "Name2", LocalDate.of(1995, 1, 1));
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(duplicate)))
                .andExpect(status().isConflict());
    }

    @Test
    public void create_emptyLogin_throwsValidationException() throws Exception {
        User user = new User(null, "user@mail.ru", "", "name", LocalDate.of(1995, 2, 13));
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(user)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void create_loginWithSpaces_throwsValidationException() throws Exception {
        User user = new User(null, "user@mail.ru", "login with space", "name", LocalDate.of(1995, 2, 13));
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(user)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void create_birthdayInFuture_throwsValidationException() throws Exception {
        User user = new User(null, "user@mail.ru", "login", "name", LocalDate.now().plusDays(1));
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(user)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void update_existingUserWithValidData_fieldsUpdated() throws Exception {
        User user = new User(null, "user@mail.ru", "login", "name", LocalDate.of(1995, 2, 13));
        String response = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(user)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        User created = objectMapper.readValue(response, User.class);

        created.setEmail("new@mail.ru");
        created.setLogin("new_login");

        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(created)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("new@mail.ru"))
                .andExpect(jsonPath("$.login").value("new_login"));
    }

    @Test
    public void update_nonExistentUserId_throwsNotFoundException() throws Exception {
        User user = new User(999L, "nonexistent@mail.ru", "login", "name", LocalDate.of(1995, 2, 13));
        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(user)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void findAll_afterAddingTwoUsers_returnsCollectionSize2() throws Exception {
        User user1 = new User(null, "user1@mail.ru", "login1", "name1", LocalDate.of(1984, 12, 13));
        User user2 = new User(null, "user2@mail.ru", "login2", "name2", LocalDate.of(1985, 12, 14));

        mockMvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON).content(toJson(user1))).andExpect(status().isOk());
        mockMvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON).content(toJson(user2))).andExpect(status().isOk());

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }
}