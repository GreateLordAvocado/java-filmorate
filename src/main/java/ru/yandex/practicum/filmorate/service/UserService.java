package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.friendship.FriendshipStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService {

    private final UserStorage userStorage;
    private final FriendshipStorage friendshipStorage;

    public UserService(@Qualifier("userDbStorage") UserStorage userStorage,
                       @Qualifier("friendshipDbStorage") FriendshipStorage friendshipStorage) {
        this.userStorage = userStorage;
        this.friendshipStorage = friendshipStorage;
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
        getById(id);
        userStorage.delete(id);
        log.info("Пользователь id={} успешно удалён", id);
    }

    public void addFriend(Long id, Long friendId) {
        log.info("Добавление в друзья (одностороннее): {} -> {}", id, friendId);
        if (Objects.equals(id, friendId)) {
            log.warn("Попытка добавить себя в друзья: userId={}", id);
            throw new ValidationException("Нельзя добавить самого себя в друзья");
        }
        getById(id);
        getById(friendId);
        friendshipStorage.request(id, friendId);
        log.debug("Заявка в друзья создана: {} -> {}", id, friendId);
    }

    public void removeFriend(Long id, Long friendId) {
        log.info("Удаление из друзей (одностороннее): {} -X-> {}", id, friendId);
        if (Objects.equals(id, friendId)) {
            log.warn("Попытка удалить из друзей самого себя: userId={}", id);
            throw new ValidationException("Нельзя удалить из друзей самого себя");
        }
        getById(id);
        getById(friendId);
        friendshipStorage.remove(id, friendId);
        log.debug("Дружба удалена: {} -X-> {}", id, friendId);
    }

    public List<User> getFriends(Long id) {
        log.info("Запрос на получение друзей пользователя id={}", id);
        getById(id);
        Set<Long> friendIds = friendshipStorage.getFriends(id, false);
        return friendIds.stream()
                .map(userStorage::getById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    public List<User> getCommonFriends(Long id, Long otherId) {
        log.info("Запрос на общих друзей: {} и {}", id, otherId);
        getById(id);
        getById(otherId);

        Set<Long> commonIds = friendshipStorage.getCommonFriends(id, otherId);

        Map<Long, User> byId = userStorage.getAll().stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        List<User> common = commonIds.stream()
                .map(byId::get)
                .filter(Objects::nonNull)
                .toList();

        log.debug("Общие друзья найдены: {}", common.size());
        return common;
    }
}