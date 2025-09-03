package ru.yandex.practicum.filmorate.storage.friendship;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;

@JdbcTest
@AutoConfigureTestDatabase
@Import({FriendshipDbStorage.class, UserDbStorage.class})
class FriendshipDbStorageTest {

    @Autowired
    private FriendshipDbStorage friendships;

    @Autowired
    private UserDbStorage users;

    private User u(String login) {
        User x = new User();
        x.setEmail(login + "@ex.com");
        x.setLogin(login);
        x.setName(login);
        x.setBirthday(LocalDate.of(1990,1,1));
        return users.create(x);
    }

    @Test
    void request_and_remove_are_oneSided() {
        User a = u("a");
        User b = u("b");

        friendships.request(a.getId(), b.getId());

        Assertions.assertThat(friendships.getFriends(a.getId(), false))
                .containsExactly(b.getId());
        Assertions.assertThat(friendships.getFriends(a.getId(), true))
                .isEmpty();

        Assertions.assertThat(friendships.getFriends(b.getId(), false))
                .isEmpty();

        friendships.remove(a.getId(), b.getId());

        Assertions.assertThat(friendships.getFriends(a.getId(), false)).isEmpty();
    }

    @Test
    void mutual_requests_confirm_both_sides_and_remove_is_oneSided() {
        User a2 = u("a2");
        User b2 = u("b2");

        friendships.request(a2.getId(), b2.getId());
        Assertions.assertThat(friendships.getFriends(a2.getId(), true)).isEmpty();
        Assertions.assertThat(friendships.getFriends(b2.getId(), true)).isEmpty();

        friendships.request(b2.getId(), a2.getId());

        Assertions.assertThat(friendships.getFriends(a2.getId(), true))
                .containsExactly(b2.getId());
        Assertions.assertThat(friendships.getFriends(b2.getId(), true))
                .containsExactly(a2.getId());

        friendships.remove(a2.getId(), b2.getId());

        Assertions.assertThat(friendships.getFriends(a2.getId(), false)).isEmpty();
        Assertions.assertThat(friendships.getFriends(b2.getId(), false))
                .containsExactly(a2.getId());
    }
}