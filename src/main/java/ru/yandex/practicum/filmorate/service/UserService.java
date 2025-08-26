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
        log.info("Добавление в друзья: пользователь id={} и пользователь id={}", id, friendId);
        User user = getById(id);
        User friend = getById(friendId);

        user.addFriend(friendId);
        friend.addFriend(id);

        userStorage.update(user);
        userStorage.update(friend);

        log.debug("Теперь у пользователя id={} всего друзей: {}", id, user.getFriends().size());
        log.debug("Теперь у пользователя id={} всего друзей: {}", friendId, friend.getFriends().size());
    }

    public void removeFriend(Long id, Long friendId) {
        log.info("Удаление из друзей: пользователь id={} и пользователь id={}", id, friendId);
        User user = getById(id);
        User friend = getById(friendId);

        user.removeFriend(friendId);
        friend.removeFriend(id);

        userStorage.update(user);
        userStorage.update(friend);

        log.debug("После удаления у пользователя id={} всего друзей: {}", id, user.getFriends().size());
        log.debug("После удаления у пользователя id={} всего друзей: {}", friendId, friend.getFriends().size());
    }

    public List<User> getFriends(Long id) {
        log.info("Запрос на получение друзей пользователя id={}", id);
        User user = getById(id);

        // Заметил что тут такая же ситуация по аналогии с методом ниже поэтому тоже решил подправить
        Map<Long, User> userMap = userStorage.getAll().stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        List<User> friends = user.getFriends().stream()
                .map(userMap::get)
                .collect(Collectors.toList());

        log.debug("Найдено друзей у пользователя id={}: {}", id, friends.size());
        return friends;
    }

    public List<User> getCommonFriends(Long id, Long otherId) {
        log.info("Запрос на общих друзей: пользователь id={} и пользователь id={}", id, otherId);
        Set<Long> friendsOfUser = getById(id).getFriends();
        Set<Long> friendsOfOther = getById(otherId).getFriends();

        // Выгрузка всех пользователей в Map для быстрого доступа
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