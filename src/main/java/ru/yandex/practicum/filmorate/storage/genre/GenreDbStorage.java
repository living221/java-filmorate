package ru.yandex.practicum.filmorate.storage.genre;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.mpa.MpaDbStorage;

import java.util.List;

@Component
public class GenreDbStorage implements GenreStorage {

    private final JdbcTemplate jdbcTemplate;
    private final Logger log = LoggerFactory.getLogger(MpaDbStorage.class);
    private final GenreMapper genreMapper;

    public GenreDbStorage(JdbcTemplate jdbcTemplate, GenreMapper genreMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.genreMapper = genreMapper;
    }

    @Override
    public List<Genre> getGenres() {

        String sql = "SELECT g.id, g.name FROM genres g ORDER BY id";

        return jdbcTemplate.query(sql, genreMapper);
    }

    @Override
    public Genre getGenreById(int id) {

        SqlRowSet genreRows = jdbcTemplate.queryForRowSet("SELECT g.id, g.name FROM genres g WHERE id = ?", id);
        if (genreRows.next()) {
            Genre genre = new Genre(
                    genreRows.getInt("id"),
                    genreRows.getString("name")
            );
            log.info("Genre found: {} {}", genre.getId(), genre.getName());
            return genre;
        } else {
            log.info("Genre with id: {} not found.", id);
            return null;
        }
    }
}
