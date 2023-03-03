package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class FilmDbStorageTest {

    private final FilmDbStorage filmDbStorage;
    private final UserDbStorage userDbStorage;

    private Film film;
    private User user;

    @BeforeEach
    public void init() {
        Set<Genre> genres = new HashSet<>();
        genres.add(new Genre(1, null));
        genres.add(new Genre(2, null));
        genres.add(new Genre(3, null));


        film = Film.builder()
                .id(1)
                .name("film name")
                .description("film description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(100)
                .mpa(new Mpa(4, null))
                .genres(genres)
                .build();

        user = User.builder()
                .id(1)
                .name("user name")
                .login("user login")
                .email("user@mail.com")
                .birthday(LocalDate.of(2010, 7, 19))
                .build();
    }

    @Test
    @DisplayName("Тестирование получения всех фильмов")
    public void testGetFilms() {
        filmDbStorage.create(film);
        List<Film> films = filmDbStorage.getFilms();

        assertEquals(films.size(), 1);
    }

    @Test
    @DisplayName("Тестирование добавления фильма")
    public void testCreateFilm() {
        Film addedFilm = filmDbStorage.create(film);

        assertEquals(addedFilm.getId(), 1);
        assertEquals(addedFilm.getName(), "film name");
        assertEquals(addedFilm.getDescription(), "film description");
        assertEquals(addedFilm.getReleaseDate(), LocalDate.of(2000, 1, 1));
        assertEquals(addedFilm.getDuration(), 100);
        assertEquals(addedFilm.getMpa().getId(), 4);
        assertEquals(addedFilm.getGenres().size(), 3);
    }

    @Test
    @DisplayName("Тестирование обновлегния фильма")
    public void testFilmUpdate() {
        Film addedFilm = filmDbStorage.create(film);
        film.setName("updated name");

        filmDbStorage.update(film);
        Optional<Film> updatedFilm = filmDbStorage.getFilmById(addedFilm.getId());

        assertThat(updatedFilm)
                .isPresent()
                .hasValueSatisfying(film ->
                        assertThat(film).hasFieldOrPropertyWithValue("name", "updated name")
                );
    }

    @Test
    @DisplayName("Тестирование поиска фильма по id")
    public void testGetFilmById() {
        Film addedFilm = filmDbStorage.create(film);

        Optional<Film> filmFromDb = filmDbStorage.getFilmById(addedFilm.getId());

        assertThat(filmFromDb)
                .isPresent()
                .hasValueSatisfying(film ->
                        assertThat(film).hasFieldOrPropertyWithValue("id", addedFilm.getId())
                );
    }

    @Test
    @DisplayName("Тестирование добавления лайка фильму")
    public void testAddLikeToFilm() {
        User addedUser = userDbStorage.create(user);

        Film addedFilm = filmDbStorage.create(film);

        filmDbStorage.addLike(addedFilm.getId(), addedUser.getId());
        Optional<Film> optFilm = filmDbStorage.getFilmById(addedFilm.getId());
        Film filmFromDb;

        if (optFilm.isPresent()) {
            filmFromDb = optFilm.get();
            assertEquals(filmFromDb.getLikes().size(), 1);
        } else {
            fail();
        }
    }

    @Test
    @DisplayName("Тестирование удаления лайка у фильма")
    public void testRemoveLikeFromFilm() {
        User addedUser = userDbStorage.create(user);

        Film addedFilm = filmDbStorage.create(film);

        filmDbStorage.addLike(addedFilm.getId(), addedUser.getId());
        filmDbStorage.removeLike(addedFilm.getId(), user.getId());

        Optional<Film> optFilm = filmDbStorage.getFilmById(addedFilm.getId());
        Film filmFromDb;

        if (optFilm.isPresent()) {
            filmFromDb = optFilm.get();
            assertEquals(filmFromDb.getLikes().size(), 0);
        } else {
            fail();
        }
    }
}