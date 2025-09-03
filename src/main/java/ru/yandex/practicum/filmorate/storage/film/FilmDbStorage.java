package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.*;
import java.util.stream.Collectors;

@Repository("filmDbStorage")
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbc;

    @Override
    public Film create(Film film) {
        final String sql = """
                INSERT INTO films (name, description, release_date, duration, mpa_id)
                VALUES (?, ?, ?, ?, ?)
                """;
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, Date.valueOf(film.getReleaseDate()));
            ps.setInt(4, film.getDuration());
            ps.setInt(5, film.getMpa().getId());
            return ps;
        }, kh);
        film.setId(Objects.requireNonNull(kh.getKey()).longValue());

        upsertGenres(film.getId(), film.getGenres());
        return getById(film.getId()).orElseThrow();
    }

    @Override
    public Film update(Film film) {
        final String sql = """
                UPDATE films
                   SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ?
                 WHERE id = ?
                """;
        int updated = jdbc.update(sql,
                film.getName(),
                film.getDescription(),
                Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId());

        if (updated == 0) {
            return film;
        }

        upsertGenres(film.getId(), film.getGenres());
        upsertLikes(film.getId(), film.getLikes());

        return getById(film.getId()).orElseThrow();
    }

    @Override
    public Optional<Film> getById(Long id) {
        final String sql = """
                SELECT f.id, f.name, f.description, f.release_date, f.duration,
                       m.id AS mpa_id, m.code AS mpa_name
                  FROM films f
                  JOIN mpa_ratings m ON m.id = f.mpa_id
                 WHERE f.id = ?
                """;
        List<Film> list = jdbc.query(sql, (rs, rowNum) -> {
            Film f = new Film();
            f.setId(rs.getLong("id"));
            f.setName(rs.getString("name"));
            f.setDescription(rs.getString("description"));
            f.setReleaseDate(rs.getDate("release_date").toLocalDate());
            f.setDuration(rs.getInt("duration"));
            f.setMpa(new Mpa(rs.getInt("mpa_id"), rs.getString("mpa_name")));
            return f;
        }, id);

        if (list.isEmpty()) return Optional.empty();

        Film f = list.get(0);
        f.setGenres(loadGenresForFilm(f.getId()));
        f.getLikes().addAll(loadLikesForFilm(f.getId()));
        return Optional.of(f);
    }

    @Override
    public List<Film> getAll() {
        final String sql = """
                SELECT f.id, f.name, f.description, f.release_date, f.duration,
                       m.id AS mpa_id, m.code AS mpa_name
                  FROM films f
                  JOIN mpa_ratings m ON m.id = f.mpa_id
                """;
        List<Film> films = jdbc.query(sql, (rs, rowNum) -> {
            Film f = new Film();
            f.setId(rs.getLong("id"));
            f.setName(rs.getString("name"));
            f.setDescription(rs.getString("description"));
            f.setReleaseDate(rs.getDate("release_date").toLocalDate());
            f.setDuration(rs.getInt("duration"));
            f.setMpa(new Mpa(rs.getInt("mpa_id"), rs.getString("mpa_name")));
            return f;
        });

        Map<Long, Set<Genre>> genresByFilm = loadAllFilmGenres();
        Map<Long, Set<Long>> likesByFilm = loadAllFilmLikes();
        for (Film f : films) {
            f.setGenres(genresByFilm.getOrDefault(f.getId(), new LinkedHashSet<>()));
            f.getLikes().addAll(likesByFilm.getOrDefault(f.getId(), Collections.emptySet()));
        }
        return films;
    }

    @Override
    public void delete(Long id) {
        jdbc.update("DELETE FROM films WHERE id = ?", id);
    }

    private Set<Genre> loadGenresForFilm(Long filmId) {
        final String sql = """
                SELECT g.id, g.name
                  FROM film_genres fg
                  JOIN genres g ON g.id = fg.genre_id
                 WHERE fg.film_id = ?
                 ORDER BY g.id
                """;
        return jdbc.query(sql, (rs, rn) -> new Genre(rs.getInt("id"), rs.getString("name")), filmId)
                .stream()
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private Map<Long, Set<Genre>> loadAllFilmGenres() {
        final String sql = """
                SELECT fg.film_id, g.id, g.name
                  FROM film_genres fg
                  JOIN genres g ON g.id = fg.genre_id
                 ORDER BY fg.film_id, g.id
                """;
        return jdbc.query(sql, rs -> {
            Map<Long, Set<Genre>> map = new HashMap<>();
            while (rs.next()) {
                long filmId = rs.getLong("film_id");
                Genre g = new Genre(rs.getInt("id"), rs.getString("name"));
                map.computeIfAbsent(filmId, k -> new LinkedHashSet<>()).add(g);
            }
            return map;
        });
    }

    private void upsertGenres(Long filmId, Set<Genre> genres) {
        jdbc.update("DELETE FROM film_genres WHERE film_id = ?", filmId);
        if (genres == null || genres.isEmpty()) return;
        final String insert = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";

        List<Object[]> batch = genres.stream()
                .filter(Objects::nonNull)
                .map(Genre::getId)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .map(gid -> new Object[]{filmId, gid})
                .toList();

        jdbc.batchUpdate(insert, batch);
    }

    private Set<Long> loadLikesForFilm(Long filmId) {
        final String sql = "SELECT user_id FROM likes WHERE film_id = ?";
        return new LinkedHashSet<>(jdbc.query(sql, (rs, rn) -> rs.getLong("user_id"), filmId));
    }

    private Map<Long, Set<Long>> loadAllFilmLikes() {
        final String sql = "SELECT film_id, user_id FROM likes";
        return jdbc.query(sql, rs -> {
            Map<Long, Set<Long>> map = new HashMap<>();
            while (rs.next()) {
                long fid = rs.getLong("film_id");
                long uid = rs.getLong("user_id");
                map.computeIfAbsent(fid, k -> new LinkedHashSet<>()).add(uid);
            }
            return map;
        });
    }

    private void upsertLikes(Long filmId, Set<Long> likes) {
        if (likes == null) return;
        jdbc.update("DELETE FROM likes WHERE film_id = ?", filmId);
        if (likes.isEmpty()) return;

        final String insert = "INSERT INTO likes (film_id, user_id) VALUES (?, ?)";
        List<Object[]> batch = likes.stream()
                .distinct()
                .map(uid -> new Object[]{filmId, uid})
                .toList();
        jdbc.batchUpdate(insert, batch);
    }
}