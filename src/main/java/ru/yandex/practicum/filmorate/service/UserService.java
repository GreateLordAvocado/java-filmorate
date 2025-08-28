package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService {
    private final UserStorage userStorage;

    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public User create(User user) {
        log.debug("Создание пользователя: {}", user);
        return userStorage.create(user);
    }

    public User update(User user) {
        log.debug("Обновление пользователя: {}", user);
        return userStorage.update(user)
                .orElseThrow(() -> {
                    log.warn("Не удалось обновить пользователя id={}", user.getId());
                    return new NotFoundException("Пользователь с id=" + user.getId() + " не найден");
                });
    }

    public User getById(Long id) {
        log.debug("Поиск пользователя по id={}", id);
        return userStorage.getById(id)
                .orElseThrow(() -> {
                    log.warn("Пользователь с id={} не найден", id);
                    return new NotFoundException("Пользователь с id=" + id + " не найден");
                });
    }

    public List<User> getAll() {
        log.debug("Запрос на получение всех пользователей");
        return userStorage.getAll();
    }

    public void delete(Long id) {
        log.info("Удаление пользователя id={}", id);
        userStorage.getById(id)
                .orElseThrow(() -> {
                    log.warn("Попытка удалить несуществующего пользователя id={}", id);
                    return new NotFoundException("Пользователь с id=" + id + " не найден");
                });
        userStorage.delete(id);
        log.info("Пользователь id={} успешно удален", id);
    }

    public void addFriend(Long id, Long friendId) {
        log.info("Добавление в друзья (одностороннее): {} -> {}", id, friendId);
        User user = getById(id);
        getById(friendId);

        user.addFriend(friendId);
        userStorage.update(user);

        log.debug("У пользователя id={} теперь друзей: {}", id, user.getFriends().size());
    }

    public void removeFriend(Long id, Long friendId) {
        log.info("Удаление из друзей (одностороннее): {} -X-> {}", id, friendId);
        User user = getById(id);
        getById(friendId); // валидация наличия пользователя

        user.removeFriend(friendId);
        userStorage.update(user);

        log.debug("После удаления у пользователя id={} друзей: {}", id, user.getFriends().size());
    }

    public List<User> getFriends(Long id) {
        log.info("Запрос на получение друзей пользователя id={}", id);
        User user = getById(id);

        Map<Long, User> userMap = userStorage.getAll().stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        List<User> friends = user.getFriends().stream()
                .map(userMap::get)
                .collect(Collectors.toList());

        log.debug("Найдено друзей у пользователя id={}: {}", id, friends.size());
        return friends;
    }

    public List<User> getCommonFriends(Long id, Long otherId) {
        log.info("Запрос на общих друзей: {} и {}", id, otherId);
        Set<Long> friendsOfUser = getById(id).getFriends();
        Set<Long> friendsOfOther = getById(otherId).getFriends();

        Map<Long, User> userMap = userStorage.getAll().stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        List<User> common = friendsOfUser.stream()
                .filter(friendsOfOther::contains)
                .map(userMap::get)
                .collect(Collectors.toList());

        log.debug("Общие друзья найдены: {}", common.size());
        return common;
    }
}