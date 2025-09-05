package ru.yandex.practicum.filmorate.storage.friendship;

import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

@Component
public class InMemoryFriendshipStorage implements FriendshipStorage {

    private final ConcurrentHashMap<Long, ConcurrentHashMap<Long, FriendshipStatus>> graph = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, ReentrantLock> locks = new ConcurrentHashMap<>();

    private ConcurrentHashMap<Long, FriendshipStatus> neighbors(Long userId) {
        return graph.computeIfAbsent(userId, k -> new ConcurrentHashMap<>());
    }

    private ReentrantLock lockFor(Long id) {
        return locks.computeIfAbsent(id, k -> new ReentrantLock());
    }

    private void withLocks(Long a, Long b, Runnable action) {
        Long first = a <= b ? a : b;
        Long second = a <= b ? b : a;

        ReentrantLock l1 = lockFor(first);
        ReentrantLock l2 = lockFor(second);

        l1.lock();
        try {
            if (!Objects.equals(first, second)) {
                l2.lock();
            }
            try {
                action.run();
            } finally {
                if (!Objects.equals(first, second)) {
                    l2.unlock();
                }
            }
        } finally {
            l1.unlock();
        }
    }

    @Override
    public void request(Long userId, Long friendId) {
        withLocks(userId, friendId, () -> {
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
        });
    }

    @Override
    public void remove(Long userId, Long friendId) {
        withLocks(userId, friendId, () -> {
            neighbors(userId).remove(friendId);
            neighbors(friendId).remove(userId);
        });
    }

    @Override
    public Set<Long> getFriends(Long userId, boolean confirmedOnly) {
        return neighbors(userId).entrySet().stream()
                .filter(e -> !confirmedOnly || e.getValue() == FriendshipStatus.CONFIRMED)
                .map(Map.Entry::getKey)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public Set<Long> getCommonFriends(Long userId, Long otherUserId) {
        Set<Long> a = getFriends(userId, true);
        Set<Long> b = getFriends(otherUserId, true);
        a.retainAll(b);
        return a;
    }

    @Override
    public Optional<FriendshipStatus> getStatus(Long userId, Long friendId) {
        return Optional.ofNullable(neighbors(userId).get(friendId));
    }
}