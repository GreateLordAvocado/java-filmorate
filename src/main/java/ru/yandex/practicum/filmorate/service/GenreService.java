package ru.yandex.practicum.filmorate.service;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;


import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


@Slf4j
@Service
public class GenreService {
    private final Map<Integer, Genre> data = new LinkedHashMap<>();


    public GenreService() {
        data.put(1, new Genre(1, "Комедия"));
        data.put(2, new Genre(2, "Драма"));
        data.put(3, new Genre(3, "Мультфильм"));
        data.put(4, new Genre(4, "Триллер"));
        data.put(5, new Genre(5, "Документальный"));
        data.put(6, new Genre(6, "Боевик"));
    }


    public List<Genre> getAll() {
        return data.values().stream()
                .sorted(Comparator.comparingInt(Genre::getId))
                .toList();
    }


    public Genre getById(int id) {
        Genre g = data.get(id);
        if (g == null) {
            log.warn("Жанр id={} не найден", id);
            throw new NotFoundException("Жанр с id=" + id + " не найден");
        }
        return g;
    }
}