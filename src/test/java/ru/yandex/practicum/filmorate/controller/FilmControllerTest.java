package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FilmControllerTest {

    private Film film;
    private FilmController controller;

    @BeforeEach
    public void init() {
        film = new Film();
        film.setId(1);
        film.setName("film name");
        film.setDescription("film description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(100);

        controller = new FilmController();
    }

    @Test
    void validateFilmWithName() {
        film.setName(null);
        ValidationException ex = assertThrows(ValidationException.class, () -> controller.createFilm(film));
        assertEquals("Film name cannot be empty!", ex.getMessage());
    }

    @Test
    void validateFilmBlankName() {
        film.setName("                    ");
        ValidationException ex = assertThrows(ValidationException.class, () -> controller.createFilm(film));
        assertEquals("Film name cannot be empty!", ex.getMessage());
    }


    @Test
    void validateFilmLongDescription() {
        film.setDescription("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nunc ullamcorper rhoncus " +
                "sem nec commodo. Nam eu sollicitudin eros. Cras nec convallis erat. Nam at venenatis sem. " +
                "Nullam augue ante efficitur. ");
        ValidationException ex = assertThrows(ValidationException.class, () -> controller.createFilm(film));
        assertEquals("Film description length cannot be more than 200 signs.", ex.getMessage());
    }


    @Test
    void validateFilmIncorrectDate() {
        film.setReleaseDate(LocalDate.of(1895, 12, 28));
        controller.films.put(film.getId(), film);
        assertEquals(controller.films.values().size(), 1);

        film.setReleaseDate(LocalDate.of(1895, 12, 27));
        ValidationException ex = assertThrows(ValidationException.class, () -> controller.createFilm(film));
        assertEquals("Film release date cannot be before 28.12.1895", ex.getMessage());
    }


    @Test
    void validateFilmDuration() {
        film.setDuration(1);
        controller.films.put(film.getId(), film);
        assertEquals(controller.films.values().size(), 1);

        film.setDuration(0);
        ValidationException ex = assertThrows(ValidationException.class, () -> controller.createFilm(film));
        assertEquals("Film duration cannot be negative or zero.", ex.getMessage());

        film.setDuration(-1);
        ex = assertThrows(ValidationException.class, () -> controller.createFilm(film));
        assertEquals("Film duration cannot be negative or zero.", ex.getMessage());
    }

    @Test
    void updateFilm() {
        controller.createFilm(film);
        assertEquals(controller.films.values().size(), 1);
        assertEquals(film.getId(), 1);

        film.setId(Integer.MIN_VALUE);
        ValidationException ex = assertThrows(ValidationException.class, () -> controller.updateFilm(film));
        assertEquals(String.format("Film with id: %s was not found!", film.getId()), ex.getMessage());
    }
}