package ru.yandex.practicum.filmorate.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exceptions.*;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class UserControllerTest {

    private UserController userController;

    @BeforeEach
    public void beforeEach() {
        userController = new UserController();
    }

    // Тест успешного создания пользователя с валидными данными
    @Test
    public void create_allRequiredFieldsValid_userAddedWithGeneratedId() {
        User user = new User();
        user.setEmail("user@mail.ru");
        user.setLogin("user_login");
        user.setBirthday(LocalDate.of(1995, 2, 13));

        User createdUser = userController.create(user);

        assertNotNull(createdUser.getId(), "Пользователю не был присвоен id");
        assertEquals(1, userController.findAll().size(),
                "Неверное количество пользователей после создания");
    }

    // Тест создания пользователя с пустым именем
    @Test
    public void create_emptyName_nameEqualsLogin() {
        User user = new User();
        user.setEmail("user@mail.ru");
        user.setLogin("user_login");
        user.setName("");
        user.setBirthday(LocalDate.of(1995, 2, 13));

        User createdUser = userController.create(user);

        assertEquals("user_login", createdUser.getName(),
                "При пустом имени должно подставляться значение login");
    }

    // Тест создания пользователя с null именем (должно подставляться значение login)
    @Test
    public void create_nullName_nameEqualsLogin() {
        User user = new User();
        user.setEmail("user@mail.ru");
        user.setLogin("user_login");
        user.setName(null);
        user.setBirthday(LocalDate.of(1995, 2, 13));

        User createdUser = userController.create(user);

        assertEquals("user_login", createdUser.getName(),
                "При null имени должно подставляться значение login");
    }

    // Тест валидации email - не может быть пустым
    @Test
    public void create_emptyEmail_throwsValidationException() {
        User user = new User();
        user.setEmail("");
        user.setLogin("login");
        user.setBirthday(LocalDate.of(1995, 2, 13));

        assertThrows(ValidationException.class, () -> userController.create(user),
                "Ожидалось ValidationException при пустом email");
    }

    // Тест валидации email - должен быть корректным форматом
    @Test
    public void create_invalidEmailFormat_throwsValidationException() {
        User user = new User();
        user.setEmail("invalid-email");
        user.setLogin("login");
        user.setBirthday(LocalDate.of(1995, 2, 13));

        assertThrows(ValidationException.class, () -> userController.create(user),
                "Ожидалось ValidationException при некорректном email");
    }

    // Тест уникальности email - нельзя создать пользователя с существующим email
    @Test
    public void create_duplicateEmail_throwsDuplicateException() {
        User user1 = new User();
        user1.setEmail("user@mail.ru");
        user1.setLogin("login1");
        user1.setBirthday(LocalDate.of(1996, 2, 14));
        userController.create(user1);

        User user2 = new User();
        user2.setEmail("user@mail.ru");
        user2.setLogin("login2");
        user2.setBirthday(LocalDate.of(1995, 2, 13));

        assertThrows(DuplicateException.class, () -> userController.create(user2),
                "Ожидалось DuplicateException при дублировании email");
    }

    // Тест добавления пользователя с email, равным null
    @Test
    public void create_nullEmail_throwsValidationException() {
        User user = new User();
        user.setEmail(null);
        user.setLogin("login");
        user.setBirthday(LocalDate.of(1995, 2, 13));

        assertThrows(NullPointerException.class, () -> userController.create(user),
                "Ожидалось NullPointerException, если email = null");
    }

    // Тест валидации логина - не может быть пустым
    @Test
    public void create_emptyLogin_throwsValidationException() {
        User user = new User();
        user.setEmail("user@mail.ru");
        user.setLogin("");
        user.setBirthday(LocalDate.of(1995, 2, 13));

        assertThrows(ValidationException.class, () -> userController.create(user),
                "Ожидалось ValidationException при пустом логине");
    }

    // Тест валидации логина - не может содержать пробелов
    @Test
    public void create_loginWithSpaces_throwsValidationException() {
        User user = new User();
        user.setEmail("user@mail.ru");
        user.setLogin("login with spaces");
        user.setBirthday(LocalDate.of(1995, 2, 13));

        assertThrows(ValidationException.class, () -> userController.create(user),
                "Ожидалось ValidationException при логине с пробелами");
    }

    // Тест валидации даты рождения - не может быть в будущем
    @Test
    public void create_birthdayInFuture_throwsValidationException() {
        User user = new User();
        user.setEmail("user@mail.ru");
        user.setLogin("login");
        user.setBirthday(LocalDate.now().plusDays(1));

        assertThrows(ValidationException.class, () -> userController.create(user),
                "Ожидалось ValidationException при дате рождения в будущем");
    }

    // Тест обновления несуществующего пользователя
    @Test
    public void update_nonExistentUserId_throwsNotFoundException() {
        User user = new User();
        user.setId(999L);
        user.setEmail("user@mail.ru");
        user.setLogin("login");
        user.setBirthday(LocalDate.of(1995, 2, 13));

        assertThrows(NotFoundException.class, () -> userController.update(user),
                "Ожидалось NotFoundException при обновлении несуществующего пользователя");
    }

    // Тест успешного обновления пользователя
    @Test
    public void update_existingUserWithValidData_fieldsUpdated() {
        // Создание пользователя
        User user = new User();
        user.setEmail("user@mail.ru");
        user.setLogin("login");
        user.setBirthday(LocalDate.of(1995, 2, 13));
        User createdUser = userController.create(user);

        // Обновление пользователя
        User updatedUser = new User();
        updatedUser.setId(createdUser.getId());
        updatedUser.setEmail("new@mail.ru");
        updatedUser.setLogin("new_login");
        updatedUser.setBirthday(LocalDate.of(1995, 2, 13));
        User result = userController.update(updatedUser);

        assertEquals("new@mail.ru", result.getEmail(), "Email не обновился");
        assertEquals("new_login", result.getLogin(), "Логин не обновился");
        assertEquals(LocalDate.of(1995, 2, 13), result.getBirthday(),
                "Дата рождения не обновилась");
    }

    // Тест получения списка всех пользователей
    @Test
    public void findAll_afterAddingTwoUsers_returnsCollectionSize2() {
        User user1 = new User();
        user1.setEmail("user1@mail.ru");
        user1.setLogin("login1");
        user1.setBirthday(LocalDate.of(1984, 12, 13));
        userController.create(user1);

        User user2 = new User();
        user2.setEmail("user2@mail.ru");
        user2.setLogin("login2");
        user2.setBirthday(LocalDate.of(1985, 12, 14));
        userController.create(user2);

        assertEquals(2, userController.findAll().size(),
                "Неверное количество пользователей в списке");
    }

    // Тест проверяет невозможность добавить пользователя равного null
    @Test
    public void create_nullUser_throwsValidationException() {
        assertThrows(NullPointerException.class,
                () -> userController.create(null),
                "Ожидалось NullPointerException при null пользователе");
    }
}