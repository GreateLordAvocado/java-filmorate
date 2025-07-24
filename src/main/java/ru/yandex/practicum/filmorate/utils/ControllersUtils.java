package ru.yandex.practicum.filmorate.utils;

import java.util.Collection;

public class ControllersUtils {
    public static Long getNextId(Collection<Long> ids) {
        long currentMaxId = ids.stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}