package ru.yandex.practicum.filmorate.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.*;

@Component
@Primary
public class FilmDbStorage implements FilmStorage {

    private final Logger log = LoggerFactory.getLogger(MpaDbStorage.class);
    private final JdbcTemplate jdbcTemplate;

    public FilmDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Film> getFilms() {

        String sql = "SELECT f.id, f.name, f.description, f.release_date, f.duration, f.rate, f.mpa_id, m.id, m.name " +
                "FROM films f JOIN mpas m ON f.mpa_id = m.id";

        SqlRowSet filmRows = jdbcTemplate.queryForRowSet(sql);
        List<Film> films = new ArrayList<>();

        while (filmRows.next()) {
            Film film = Film.builder()
                    .id(filmRows.getInt("id"))
                    .name(filmRows.getString("name"))
                    .description(filmRows.getString("description"))
                    .releaseDate(filmRows.getDate("release_date").toLocalDate())
                    .duration(filmRows.getInt("duration"))
                    .rate(filmRows.getInt("rate"))
                    .build();
            films.add(film);
        }

        for (Film film : films) {
            film.setMpa(getMpaById(film.getId()));
            film.setGenres(getGenresById(film.getId()));
        }

        return films;
    }

    @Override
    public Film create(Film film) {

        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("films")
                .usingGeneratedKeyColumns("id");

        int filmId = simpleJdbcInsert.executeAndReturnKey(film.toMap()).intValue();
        film.setId(filmId);

        String sql = "insert into film_genres (film_id, film_genre_id) values (?, ?)";
        Set<Genre> genres = film.getGenres();
        if (genres != null) {
            for (Genre genre : genres) {
                jdbcTemplate.update(sql, filmId, genre.getId());
            }
        }
        film.setGenres(getGenresById(filmId));
        film.setMpa(getMpaById(film.getId()));
        return film;
    }

    @Override
    public Film update(Film film) {

        String sql = "update films set name = ?, description = ?, " +
                "release_date = ?, duration = ?, rate = ?, mpa_id = ? " +
                "where id = ?";

        int update = jdbcTemplate.update(sql,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getRate(),
                film.getMpa().getId(),
                film.getId());

        if (update > 0) {
            film.setMpa(getMpaById(film.getId()));
            film.setGenres(getGenresById(film.getId()));
            return film;
        } else {
            return null;
        }
    }

    @Override
    public Optional<Film> getFilmById(int filmId) {

        String sql = "SELECT * FROM FILMS WHERE ID = ?";

        Optional<Film> film = Optional.ofNullable(jdbcTemplate.queryForObject(sql, new FilmMapper(), filmId));
        film.ifPresent(value -> value.setMpa(getMpaById(filmId)));
        film.ifPresent(value -> value.setGenres(getGenresById(filmId)));
        return film;
    }

    private Set<Genre> getGenresById(int filmId) {
        String genresSql = "SELECT g.id, g.name FROM genres g " +
                "JOIN film_genres fg ON g.id = fg.film_genre_id " +
                "WHERE fg.film_id = ?";

        SqlRowSet genresRowSet = jdbcTemplate.queryForRowSet(genresSql, filmId);
        Set<Genre> filmGenres = new HashSet<>();
        while (genresRowSet.next()) {
            Genre genre = new Genre(genresRowSet.getInt("id"),
                    genresRowSet.getString("name"));
            filmGenres.add(genre);
        }
        return filmGenres;
    }

    private Mpa getMpaById(int filmId) {
        String mpaSql = "SELECT m.id, m.name FROM mpas m " +
                "JOIN films f ON f.mpa_id = m.id " +
                "WHERE f.id = ?";

        return jdbcTemplate.queryForObject(mpaSql, new MpaMapper(), filmId);
    }
}
