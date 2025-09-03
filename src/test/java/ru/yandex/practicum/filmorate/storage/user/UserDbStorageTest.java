package ru.yandex.practicum.filmorate.storage.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import(UserDbStorage.class)
class UserDbStorageTest {

    @Autowired
    private UserDbStorage userStorage;

    private User makeUser(String login, String email) {
        User u = new User();
        u.setLogin(login);
        u.setEmail(email);
        u.setName("");
        u.setBirthday(LocalDate.of(1990, 1, 1));
        return u;
    }

    @Test
    void createAndFindById() {
        User saved = userStorage.create(makeUser("john", "john@example.com"));
        Optional<User> found = userStorage.getById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(saved.getId());
        assertThat(found.get().getEmail()).isEqualTo("john@example.com");
        assertThat(found.get().getLogin()).isEqualTo("john");
        assertThat(found.get().getName()).isEqualTo("john");
    }

    @Test
    void updateUser() {
        User saved = userStorage.create(makeUser("mike", "mike@example.com"));
        saved.setName("Michael");
        saved.setEmail("michael@example.com");

        Optional<User> updated = userStorage.update(saved);
        assertThat(updated).isPresent();

        User reloaded = userStorage.getById(saved.getId()).orElseThrow();
        assertThat(reloaded.getName()).isEqualTo("Michael");
        assertThat(reloaded.getEmail()).isEqualTo("michael@example.com");
    }

    @Test
    void upsertFriendsOneSided() {
        User u1 = userStorage.create(makeUser("a", "a@ex.com"));
        User u2 = userStorage.create(makeUser("b", "b@ex.com"));

        u1.getFriends().add(u2.getId());
        userStorage.update(u1);

        User reloaded = userStorage.getById(u1.getId()).orElseThrow();
        assertThat(reloaded.getFriends()).containsExactly(u2.getId());

        User reloaded2 = userStorage.getById(u2.getId()).orElseThrow();
        assertThat(reloaded2.getFriends()).isEmpty();
    }
}