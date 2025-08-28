package ru.yandex.practicum.filmorate.storage.friendship;

import java.util.Optional;
import java.util.Set;

public interface FriendshipStorage {

    void request(Long userId, Long friendId);

    void remove(Long userId, Long friendId);

    Set<Long> getFriends(Long userId, boolean confirmedOnly);

    Set<Long> getCommonFriends(Long userId, Long otherUserId);

    Optional<FriendshipStatus> getStatus(Long userId, Long friendId);
}