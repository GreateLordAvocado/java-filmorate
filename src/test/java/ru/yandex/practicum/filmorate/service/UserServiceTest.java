package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    private UserStorage userStorage;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userStorage = mock(UserStorage.class);
        userService = new UserService(userStorage);
    }

    private User createUser(Long id, String login) {
        User user = new User();
        user.setId(id);
        user.setLogin(login);
        user.setEmail(login + "@mail.com");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return user;
    }

    @Test
    void shouldAddFriend() {
        User user1 = createUser(1L, "user1");
        User user2 = createUser(2L, "user2");

        when(userStorage.getById(1L)).thenReturn(Optional.of(user1));
        when(userStorage.getById(2L)).thenReturn(Optional.of(user2));

        userService.addFriend(1L, 2L);

        assertTrue(user1.getFriends().contains(2L));
    }

    @Test
    void shouldRemoveFriend() {
        User user1 = createUser(1L, "user1");
        user1.getFriends().add(2L);

        User user2 = createUser(2L, "user2");

        when(userStorage.getById(1L)).thenReturn(Optional.of(user1));
        when(userStorage.getById(2L)).thenReturn(Optional.of(user2));

        userService.removeFriend(1L, 2L);

        assertFalse(user1.getFriends().contains(2L));
    }

    @Test
    void shouldReturnFriendsList() {
        User user1 = createUser(1L, "user1");
        User user2 = createUser(2L, "user2");
        User user3 = createUser(3L, "user3");

        user1.getFriends().addAll(Set.of(2L, 3L));

        when(userStorage.getById(1L)).thenReturn(Optional.of(user1));
        when(userStorage.getAll()).thenReturn(List.of(user1, user2, user3)); // 🔥 добавили

        List<User> friends = userService.getFriends(1L);

        assertEquals(2, friends.size());
        assertTrue(friends.contains(user2));
        assertTrue(friends.contains(user3));
    }

    @Test
    void shouldReturnCommonFriends() {
        User user1 = createUser(1L, "user1");
        User user2 = createUser(2L, "user2");
        User user3 = createUser(3L, "user3");

        user1.getFriends().add(3L);
        user2.getFriends().add(3L);

        when(userStorage.getById(1L)).thenReturn(Optional.of(user1));
        when(userStorage.getById(2L)).thenReturn(Optional.of(user2));
        when(userStorage.getById(3L)).thenReturn(Optional.of(user3));

        when(userStorage.getAll()).thenReturn(List.of(user1, user2, user3));

        List<User> common = userService.getCommonFriends(1L, 2L);

        assertEquals(1, common.size());
        assertEquals(user3, common.get(0));
    }
}