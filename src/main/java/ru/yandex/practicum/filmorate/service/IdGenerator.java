package ru.yandex.practicum.filmorate.service;

import java.util.Collection;

public class IdGenerator {
    public Long getNextId(Collection<Long> ids) {
        long currentMaxId = ids.stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}