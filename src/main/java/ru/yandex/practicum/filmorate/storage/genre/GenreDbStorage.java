package ru.yandex.practicum.filmorate.storage.genre;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.Optional;

@Repository("genreDbStorage")
@RequiredArgsConstructor
public class GenreDbStorage implements GenreStorage {

    private final JdbcTemplate jdbc;

    @Override
    public List<Genre> getAll() {
        final String sql = "SELECT id, name FROM genres ORDER BY id";
        return jdbc.query(sql, (rs, rn) -> new Genre(rs.getInt("id"), rs.getString("name")));
    }

    @Override
    public Optional<Genre> getById(int id) {
        final String sql = "SELECT id, name FROM genres WHERE id = ?";
        List<Genre> list = jdbc.query(sql, (rs, rn) -> new Genre(rs.getInt("id"), rs.getString("name")), id);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }
}