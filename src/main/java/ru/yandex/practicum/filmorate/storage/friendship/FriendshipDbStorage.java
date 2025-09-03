package ru.yandex.practicum.filmorate.storage.friendship;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository("friendshipDbStorage")
@RequiredArgsConstructor
public class FriendshipDbStorage implements FriendshipStorage {

    private final JdbcTemplate jdbc;

    @Override
    public void request(Long userId, Long friendId) {
        Integer alreadyConfirmed = jdbc.queryForObject(
                "SELECT COUNT(*) FROM friendships WHERE user_id=? AND friend_id=? AND is_confirmed=TRUE",
                Integer.class, userId, friendId
        );
        if (alreadyConfirmed != null && alreadyConfirmed > 0) {
            return;
        }

        Integer reversePending = jdbc.queryForObject(
                "SELECT COUNT(*) FROM friendships WHERE user_id=? AND friend_id=? AND is_confirmed=FALSE",
                Integer.class, friendId, userId
        );

        if (reversePending != null && reversePending > 0) {
            jdbc.update(
                    "MERGE INTO friendships (user_id, friend_id, is_confirmed, requested_at, confirmed_at) " +
                            "KEY(user_id, friend_id) VALUES (?, ?, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
                    userId, friendId
            );
            jdbc.update(
                    "UPDATE friendships SET is_confirmed=TRUE, confirmed_at=CURRENT_TIMESTAMP " +
                            "WHERE user_id=? AND friend_id=?",
                    friendId, userId
            );
        } else {
            jdbc.update(
                    "MERGE INTO friendships (user_id, friend_id, is_confirmed, requested_at) " +
                            "KEY(user_id, friend_id) VALUES (?, ?, FALSE, CURRENT_TIMESTAMP)",
                    userId, friendId
            );
        }
    }

    @Override
    public void remove(Long userId, Long friendId) {
        jdbc.update("DELETE FROM friendships WHERE user_id=? AND friend_id=?", userId, friendId);
    }

    @Override
    public Set<Long> getFriends(Long userId, boolean confirmedOnly) {
        String sql = confirmedOnly
                ? "SELECT friend_id FROM friendships WHERE user_id=? AND is_confirmed=TRUE ORDER BY friend_id"
                : "SELECT friend_id FROM friendships WHERE user_id=? ORDER BY friend_id";
        List<Long> ids = jdbc.query(sql, (rs, rn) -> rs.getLong(1), userId);
        return new LinkedHashSet<>(ids);
    }

    @Override
    public Set<Long> getCommonFriends(Long userId, Long otherUserId) {
        List<Long> ids = jdbc.query(
                "SELECT f1.friend_id " +
                        "FROM friendships f1 " +
                        "JOIN friendships f2 ON f1.friend_id = f2.friend_id " +
                        "WHERE f1.user_id=? AND f2.user_id=? " +
                        "ORDER BY f1.friend_id",
                (rs, rn) -> rs.getLong(1),
                userId, otherUserId
        );
        return new LinkedHashSet<>(ids);
    }

    @Override
    public Optional<FriendshipStatus> getStatus(Long userId, Long friendId) {
        List<Boolean> list = jdbc.query(
                "SELECT is_confirmed FROM friendships WHERE user_id=? AND friend_id=?",
                (rs, rn) -> rs.getBoolean(1),
                userId, friendId
        );
        if (list.isEmpty()) return Optional.empty();
        return Optional.of(list.get(0) ? FriendshipStatus.CONFIRMED : FriendshipStatus.PENDING);
    }
}