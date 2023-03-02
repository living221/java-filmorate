package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.exceptions.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.UserDbStorage;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class FilmControllerTest {

    private final UserDbStorage userStorage;
    private final FilmDbStorage filmStorage;
    private Film film;
    @Autowired
    private FilmController controller;

    @BeforeEach
    public void init() {
        film = Film.builder()
                .id(1)
                .name("film name")
                .description("film description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(100)
                .build();
    }

    @Test
    @DisplayName("Тестирование фильма с названием null")
    void validateFilmWithName() {
        film.setName(null);
        ValidationException ex = assertThrows(ValidationException.class, () -> controller.createFilm(film));

        assertEquals("Film name cannot be empty!", ex.getMessage());
    }

    @Test
    @DisplayName("Тестирование фильма с пустым названием только из пробелов")
    void validateFilmBlankName() {
        film.setName("                    ");
        ValidationException ex = assertThrows(ValidationException.class, () -> controller.createFilm(film));

        assertEquals("Film name cannot be empty!", ex.getMessage());
    }

    @Test
    @DisplayName("Тестирование фильма с пустым названием")
    void validateFilmEmptyName() {
        film.setName("");
        ValidationException ex = assertThrows(ValidationException.class, () -> controller.createFilm(film));

        assertEquals("Film name cannot be empty!", ex.getMessage());
    }


    @Test
    @DisplayName("Тестирование длинны описания фильма.")
    void validateFilmLongDescription() {
        film.setDescription("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nunc ullamcorper rhoncus " +
                "sem nec commodo. Nam eu sollicitudin eros. Cras nec convallis erat. Nam at venenatis sem. " +
                "Nullam augue ante efficitur. ");
        ValidationException ex = assertThrows(ValidationException.class, () -> controller.createFilm(film));

        assertEquals("Film description length cannot be more than 200 signs.", ex.getMessage());
    }


    @Test
    @DisplayName("Тестирование даты релиза фильма")
    void validateFilmIncorrectDate() {
        film.setReleaseDate(LocalDate.of(1895, 12, 28));
        controller.createFilm(film);

        assertEquals(controller.getFilms().size(), 1);

        film.setReleaseDate(LocalDate.of(1895, 12, 27));
        ValidationException ex = assertThrows(ValidationException.class, () -> controller.createFilm(film));

        assertEquals("Film release date cannot be before 28.12.1895", ex.getMessage());
    }


    @Test
    @DisplayName("Тестирование длительности фильма")
    void validateFilmDuration() {
        film.setDuration(1);
        controller.createFilm(film);

        assertEquals(controller.getFilms().size(), 1);

        film.setDuration(0);
        ValidationException ex = assertThrows(ValidationException.class, () -> controller.createFilm(film));

        assertEquals("Film duration cannot be negative or zero.", ex.getMessage());

        film.setDuration(-1);
        ex = assertThrows(ValidationException.class, () -> controller.createFilm(film));

        assertEquals("Film duration cannot be negative or zero.", ex.getMessage());
    }

    @Test
    @DisplayName("Тестирование обновления данных фильма")
    void updateFilm() {
        controller.createFilm(film);

        assertEquals(controller.getFilms().size(), 1);
        assertEquals(film.getId(), 1);

        film.setId(Integer.MIN_VALUE);
        FilmNotFoundException ex = assertThrows(FilmNotFoundException.class, () -> controller.updateFilm(film));

        assertEquals(String.format("Film with id: %s was not found!", film.getId()), ex.getMessage());
    }
}