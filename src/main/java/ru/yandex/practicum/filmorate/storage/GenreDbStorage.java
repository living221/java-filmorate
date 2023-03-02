package ru.yandex.practicum.filmorate.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;

@Component
public class GenreDbStorage implements GenreStorage {

    private final JdbcTemplate jdbcTemplate;
    private final Logger log = LoggerFactory.getLogger(MpaDbStorage.class);

    public GenreDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Genre> getGenres() {

        String sql = "select * from genres order by id";

        return jdbcTemplate.query(sql, new GenreMapper());

//        SqlRowSet genreRows = jdbcTemplate.queryForRowSet("select * from genres order by id");
//        List<Genre> genres = new ArrayList<>();
//        while (genreRows.next()) {
//            Genre genre = new Genre(
//                    genreRows.getInt("id"),
//                    genreRows.getString("name")
//            );
//            log.info("Найден жанр фильма: {} {}", genre.getId(), genre.getName());
//            genres.add(genre);
//        }
//        return genres;
    }

    @Override
    public Genre getGenreById(int id) {

//        String sql = "select * from genres where id = ?";
//
//        return jdbcTemplate.queryForObject(sql, new GenreMapper(), id);

        SqlRowSet genreRows = jdbcTemplate.queryForRowSet("select * from genres where id = ?", id);
        if (genreRows.next()) {
            Genre genre = new Genre(
                    genreRows.getInt("id"),
                    genreRows.getString("name")
            );
            log.info("Найден жанр фильма: {} {}", genre.getId(), genre.getName());
            return genre;
        } else {
            log.info("Жанр с идентификатором {} не найден.", id);
            return null;
        }
    }
}
