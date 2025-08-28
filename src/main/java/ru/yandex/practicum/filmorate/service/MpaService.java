package ru.yandex.practicum.filmorate.service;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;


import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


@Slf4j
@Service
public class MpaService {
    private final Map<Integer, Mpa> data = new LinkedHashMap<>();


    public MpaService() {
        data.put(1, new Mpa(1, "G"));
        data.put(2, new Mpa(2, "PG"));
        data.put(3, new Mpa(3, "PG-13"));
        data.put(4, new Mpa(4, "R"));
        data.put(5, new Mpa(5, "NC-17"));
    }


    public List<Mpa> getAll() {
        return data.values().stream()
                .sorted(Comparator.comparingInt(Mpa::getId))
                .toList();
    }


    public Mpa getById(int id) {
        Mpa mpa = data.get(id);
        if (mpa == null) {
            log.warn("MPA id={} не найден", id);
            throw new NotFoundException("MPA с id=" + id + " не найден");
        }
        return mpa;
    }
}