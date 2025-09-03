package ru.yandex.practicum.filmorate.storage.mpa;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;
import java.util.Optional;

@Repository("mpaDbStorage")
@RequiredArgsConstructor
public class MpaDbStorage implements MpaStorage {

    private final JdbcTemplate jdbc;

    @Override
    public List<Mpa> getAll() {
        final String sql = "SELECT id, code FROM mpa_ratings ORDER BY id";
        return jdbc.query(sql, (rs, rn) -> new Mpa(rs.getInt("id"), rs.getString("code")));
    }

    @Override
    public Optional<Mpa> getById(int id) {
        final String sql = "SELECT id, code FROM mpa_ratings WHERE id = ?";
        List<Mpa> list = jdbc.query(sql, (rs, rn) -> new Mpa(rs.getInt("id"), rs.getString("code")), id);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }
}