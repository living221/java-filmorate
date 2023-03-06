package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.mpa.MpaMapper;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Primary
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;
    private final FilmMapper filmMapper;

    public FilmDbStorage(JdbcTemplate jdbcTemplate, FilmMapper filmMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.filmMapper = filmMapper;
    }

    @Override
    public List<Film> getFilms() {

        String sql = "SELECT f.id, f.name, f.description, f.release_date, f.duration, f.rate, f.mpa_id, m.id, m.name " +
                "FROM films f JOIN mpas m ON f.mpa_id = m.id";

        List<Film> films = jdbcTemplate.query(sql, filmMapper);

        for (Film film : films) {
            film.setMpa(getMpaById(film.getId()));
            film.setGenres(getGenresById(film.getId()));
            film.setLikes(getLikesByFilmId(film.getId()));
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

        String sqlGenres = "INSERT INTO film_genres (film_id, film_genre_id) VALUES (?, ?)";
        Set<Genre> genres = film.getGenres();
        if (genres != null) {
            for (Genre genre : genres) {
                jdbcTemplate.update(sqlGenres, filmId, genre.getId());
            }
            film.setGenres(getGenresById(filmId));
        }

        String sqlLikes = "INSERT INTO likes (user_id, film_id) VALUES (?, ?)";
        Set<Integer> likes = film.getLikes();
        if (likes != null) {
            for (Integer userId : likes) {
                jdbcTemplate.update(sqlLikes, userId, filmId);
            }
            film.setLikes(getLikesByFilmId(filmId));
        }

        if (film.getMpa() != null) {
            film.setMpa(getMpaById(film.getId()));
        }
        return film;
    }

    @Override
    public Film update(Film film) {

        String sql = "UPDATE films SET name = ?, description = ?, " +
                "release_date = ?, duration = ?, rate = ?, mpa_id = ? " +
                "WHERE id = ?";

        int update = jdbcTemplate.update(sql,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getRate(),
                film.getMpa().getId(),
                film.getId());

        if (film.getGenres() != null) {
            updateGenres(film);
        }

        if (film.getLikes() != null) {
            updateLikes(film);
        }

        if (update > 0) {
            film.setMpa(getMpaById(film.getId()));
            film.setGenres(getGenresById(film.getId()));
            return film;
        } else {
            return null;
        }
    }

    private void updateLikes(Film film) {
        Set<Integer> oldLikes = getLikesByFilmId(film.getId());
        String sqlDeleteLikes = "DELETE FROM likes WHERE film_id = ?";
        oldLikes.forEach(filmLike -> jdbcTemplate.update(sqlDeleteLikes, film.getId()));

        Set<Integer> newLikes = film.getLikes();
        String sqlUpdateLikes = "INSERT INTO likes (user_id, film_id) VALUES (?, ?)";
        newLikes.forEach(userId -> jdbcTemplate.update(sqlUpdateLikes, userId, film.getId()));
    }

    private void updateGenres(Film film) {
        Set<Integer> oldGenreIds = getGenresById(film.getId()).stream().map(Genre::getId).collect(Collectors.toSet());
        String sqlDeleteGenres = "DELETE FROM film_genres WHERE film_id = ?";
        oldGenreIds.forEach(filmGenreId -> jdbcTemplate.update(sqlDeleteGenres, film.getId()));

        List<Integer> filmGenreIds = film.getGenres().stream().map(Genre::getId).collect(Collectors.toList());
        String sqlUpdateGenres = "INSERT INTO film_genres (film_id, film_genre_id) VALUES (?, ?)";
        filmGenreIds.forEach(filmGenreId -> jdbcTemplate.update(sqlUpdateGenres, film.getId(), filmGenreId));
    }

    @Override
    public Optional<Film> getFilmById(int filmId) {

        String sql = "SELECT f.id, f.name, f.description, f.release_date, f.duration, f.rate, f.mpa_id " +
                "FROM films f WHERE ID = ?";

        Optional<Film> film = Optional.ofNullable(jdbcTemplate.queryForObject(sql, filmMapper, filmId));
        film.ifPresent(value -> value.setMpa(getMpaById(filmId)));
        film.ifPresent(value -> value.setGenres(getGenresById(filmId)));
        film.ifPresent(value -> value.setLikes(getLikesByFilmId(filmId)));
        return film;
    }

    @Override
    public void addLike(int filmId, int userId) {
        String sql = "INSERT INTO likes (user_id, film_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, userId, filmId);
    }

    @Override
    public void removeLike(int filmId, int userId) {
        String sql = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";
        jdbcTemplate.update(sql, filmId, userId);
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

    private Set<Integer> getLikesByFilmId(int filmId) {
        String sql = "SELECT l.user_id FROM films f JOIN likes l ON f.id = l.film_id WHERE f.id = ?";

        SqlRowSet likesRowSet = jdbcTemplate.queryForRowSet(sql, filmId);
        Set<Integer> likes = new HashSet<>();

        while (likesRowSet.next()) {
            likes.add(likesRowSet.getInt("user_id"));
        }
        return likes;
    }
}
