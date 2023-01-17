package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {

    private int generatorId = 0;

    protected final HashMap<Integer, Film> films = new HashMap<>();

    @GetMapping
    public List<Film> findAll() {

        return new ArrayList<>(films.values());
    }

    @PostMapping
    public Film createFilm(@RequestBody Film film) {
        filmValidation(film);

        film.setId(generateId());
        films.put(film.getId(), film);
        log.info("new film was added {}", film);

        return film;
    }

    @PutMapping
    public Film updateFilm(@RequestBody Film film) {
        filmValidation(film);

        if (films.containsKey(film.getId())) {
            films.put(film.getId(), film);
            log.info("film with id {} was updated. film: {}", film.getId(), film);
            return film;
        } else {
            log.debug("film update error: film with id {} was attempted to update.", film.getId());
            throw new ValidationException(String.format("Film with id: %s was not found!", film.getId()));
        }
    }

    private int generateId() {
        generatorId++;
        return generatorId;
    }

    private void filmValidation(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            log.debug("film validation error: film with name {} was attempted to add.", film.getName());
            throw new ValidationException("Film name cannot be empty!");
        }
        if (film.getDescription().length() > 200) {
            log.debug("film validation error: film with description {} was attempted to add.", film.getDescription());
            throw new ValidationException("Film description length cannot be more than 200 signs.");
        }
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            log.debug("film validation error: film with release date {} was attempted to add.", film.getReleaseDate());
            throw new ValidationException("Film release date cannot be before 28.12.1895");
        }
        if (film.getDuration() <= 0) {
            log.debug("film validation error: film with duration {} was attempted to add.", film.getDuration());
            throw new ValidationException("Film duration cannot be negative or zero.");
        }
    }
}
