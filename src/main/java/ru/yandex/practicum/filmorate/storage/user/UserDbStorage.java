package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.*;
import java.util.stream.Collectors;

@Repository("userDbStorage")
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbc;

    @Override
    public User create(User user) {
        final String sql = """
            INSERT INTO users (email, login, name, birthday)
            VALUES (?, ?, ?, ?)
            """;
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getLogin());
            ps.setString(3, (user.getName() == null || user.getName().isBlank()) ? user.getLogin() : user.getName());
            ps.setDate(4, Date.valueOf(user.getBirthday()));
            return ps;
        }, kh);
        user.setId(Objects.requireNonNull(kh.getKey()).longValue());

        upsertFriends(user.getId(), user.getFriends());

        return getById(user.getId()).orElseThrow();
    }

    @Override
    public Optional<User> update(User user) {
        final String sql = """
            UPDATE users
               SET email = ?, login = ?, name = ?, birthday = ?
             WHERE id = ?
            """;
        int updated = jdbc.update(sql,
                user.getEmail(),
                user.getLogin(),
                (user.getName() == null || user.getName().isBlank()) ? user.getLogin() : user.getName(),
                Date.valueOf(user.getBirthday()),
                user.getId());

        if (updated == 0) {
            return Optional.empty();
        }

        upsertFriends(user.getId(), user.getFriends());

        return getById(user.getId());
    }

    @Override
    public Optional<User> getById(Long id) {
        final String sql = """
            SELECT id, email, login, name, birthday
              FROM users
             WHERE id = ?
            """;
        List<User> list = jdbc.query(sql, (rs, rowNum) -> {
            User u = new User();
            u.setId(rs.getLong("id"));
            u.setEmail(rs.getString("email"));
            u.setLogin(rs.getString("login"));
            u.setName(rs.getString("name"));
            u.setBirthday(rs.getDate("birthday").toLocalDate());
            return u;
        }, id);

        if (list.isEmpty()) {
            return Optional.empty();
        }

        User u = list.get(0);
        u.getFriends().addAll(loadFriendsIds(u.getId()));
        return Optional.of(u);
    }

    @Override
    public List<User> getAll() {
        final String sql = """
            SELECT id, email, login, name, birthday
              FROM users
            """;
        List<User> users = jdbc.query(sql, (rs, rowNum) -> {
            User u = new User();
            u.setId(rs.getLong("id"));
            u.setEmail(rs.getString("email"));
            u.setLogin(rs.getString("login"));
            u.setName(rs.getString("name"));
            u.setBirthday(rs.getDate("birthday").toLocalDate());
            return u;
        });

        Map<Long, Set<Long>> allFriends = loadAllFriends();
        for (User u : users) {
            Set<Long> ids = allFriends.getOrDefault(u.getId(), Collections.emptySet());
            u.getFriends().addAll(ids);
        }
        return users;
    }

    @Override
    public void delete(Long id) {
        jdbc.update("DELETE FROM users WHERE id = ?", id);
    }

    private Set<Long> loadFriendsIds(Long userId) {
        final String sql = "SELECT friend_id FROM friendships WHERE user_id = ?";
        return new LinkedHashSet<>(jdbc.query(sql, (rs, rn) -> rs.getLong("friend_id"), userId));
    }

    private Map<Long, Set<Long>> loadAllFriends() {
        final String sql = "SELECT user_id, friend_id FROM friendships";
        return jdbc.query(sql, rs -> {
            Map<Long, Set<Long>> map = new HashMap<>();
            while (rs.next()) {
                long uid = rs.getLong("user_id");
                long fid = rs.getLong("friend_id");
                map.computeIfAbsent(uid, k -> new LinkedHashSet<>()).add(fid);
            }
            return map;
        });
    }

    private void upsertFriends(Long userId, Set<Long> newFriends) {
        jdbc.update("DELETE FROM friendships WHERE user_id = ?", userId);
        if (newFriends == null || newFriends.isEmpty()) {
            return;
        }

        final String insert = "INSERT INTO friendships (user_id, friend_id, is_confirmed) VALUES (?, ?, FALSE)";
        List<Object[]> batch = newFriends.stream()
                .distinct()
                .map(fid -> new Object[]{userId, fid})
                .collect(Collectors.toList());
        jdbc.batchUpdate(insert, batch);
    }
}