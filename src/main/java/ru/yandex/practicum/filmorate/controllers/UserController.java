package ru.yandex.practicum.filmorate.controllers;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exceptions.DuplicateException;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import jakarta.validation.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.IdGenerator;

import java.util.*;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    private final Map<Long, User> users = new HashMap<>();
    private final Set<String> userEmails = new HashSet<>();
    private final IdGenerator idGenerator = new IdGenerator();

    @GetMapping
    public Collection<User> findAll() {
        log.info("Получен запрос на вывод всех пользователей");
        return users.values();
    }

    @PostMapping
    public User create(@Valid @RequestBody User newUser) {
        if (newUser == null) {
            log.error("Попытка добавить null вместо пользователя");
            throw new ValidationException("Пользователь не может быть null");
        }

        if (newUser.getName() == null || newUser.getName().isBlank()) {
            newUser.setName(newUser.getLogin());
        }

        String emailLower = newUser.getEmail().toLowerCase();
        checkEmailUniqueness(emailLower);

        newUser.setId(idGenerator.getNextId(users.keySet()));
        users.put(newUser.getId(), newUser);
        userEmails.add(emailLower); // добавляем только после успешного добавления

        log.info("Пользователь {} успешно добавлен", newUser.getName());
        return newUser;
    }

    @PutMapping
    public User update(@Valid @RequestBody User user) {
        if (user == null) {
            log.error("Попытка обновить null вместо пользователя");
            throw new ValidationException("Пользователь не может быть null");
        }

        if (!users.containsKey(user.getId())) {
            log.error("Пользователь с id:{} не найден", user.getId());
            throw new NotFoundException("Пользователь с заданным id не существует");
        }

        final User oldUser = users.get(user.getId());
        if (!oldUser.getEmail().equalsIgnoreCase(user.getEmail())) {
            userEmails.remove(oldUser.getEmail().toLowerCase());
            checkEmailUniqueness(user.getEmail());
            userEmails.add(user.getEmail().toLowerCase());
        }

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

        users.put(user.getId(), user);
        log.info("Пользователь {} c id:{} успешно обновлен", user.getName(), user.getId());
        return user;
    }

    private void checkEmailUniqueness(String email) {
        if (email == null) {
            throw new NullPointerException("Email не может быть null");
        }
        if (userEmails.contains(email.toLowerCase())) {
            log.warn("Пользователь с email:{} уже был добавлен", email);
            throw new DuplicateException("Email уже используется");
        }
    }
}