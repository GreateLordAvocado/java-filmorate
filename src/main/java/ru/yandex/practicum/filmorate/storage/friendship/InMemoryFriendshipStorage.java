package ru.yandex.practicum.filmorate.storage.friendship;

import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class InMemoryFriendshipStorage implements FriendshipStorage {

    private final Map<Long, Map<Long, FriendshipStatus>> graph = new HashMap<>();

    private Map<Long, FriendshipStatus> neighbors(Long userId) {
        return graph.computeIfAbsent(userId, k -> new HashMap<>());
    }

    @Override
    public synchronized void request(Long userId, Long friendId) {
        FriendshipStatus current = neighbors(userId).get(friendId);
        if (current == FriendshipStatus.CONFIRMED) {
            return;
        }
        FriendshipStatus reverse = neighbors(friendId).get(userId);

        if (reverse == FriendshipStatus.PENDING) {
            neighbors(userId).put(friendId, FriendshipStatus.CONFIRMED);
            neighbors(friendId).put(userId, FriendshipStatus.CONFIRMED);
        } else {
            neighbors(userId).put(friendId, FriendshipStatus.PENDING);
        }
    }

    @Override
    public synchronized void remove(Long userId, Long friendId) {
        neighbors(userId).remove(friendId);
        neighbors(friendId).remove(userId);
    }

    @Override
    public synchronized Set<Long> getFriends(Long userId, boolean confirmedOnly) {
        return neighbors(userId).entrySet().stream()
                .filter(e -> !confirmedOnly || e.getValue() == FriendshipStatus.CONFIRMED)
                .map(Map.Entry::getKey)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public synchronized Set<Long> getCommonFriends(Long userId, Long otherUserId) {
        Set<Long> a = new HashSet<>(getFriends(userId, true));
        Set<Long> b = getFriends(otherUserId, true);
        a.retainAll(b);
        return a;
    }

    @Override
    public synchronized Optional<FriendshipStatus> getStatus(Long userId, Long friendId) {
        return Optional.ofNullable(neighbors(userId).get(friendId));
    }
}