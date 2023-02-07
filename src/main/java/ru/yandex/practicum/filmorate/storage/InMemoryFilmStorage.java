package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.*;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {

    private final Map<Integer, Film> films = new HashMap<>();

    private int generatorId = 0;

    private int generateId() {
        return ++generatorId;
    }

    @Override
    public List<Film> getFilms() {
        return new ArrayList<>(films.values());
    }

    @Override
    public Film create(Film film) {

        filmValidation(film);

        film.setId(generateId());
        films.put(film.getId(), film);
        log.info("new film was added {}", film);

        return film;
    }

    @Override
    public Film update(Film film) {

        filmValidation(film);

        if (films.containsKey(film.getId())) {
            films.put(film.getId(), film);
            log.info("film with id {} was updated. film: {}", film.getId(), film);
            return film;
        } else {
            log.error("film update error: film with id {} was attempted to update.", film.getId());
            throw new FilmNotFoundException(String.format("Film with id: %s was not found!", film.getId()));
        }

    }

    @Override
    public Film getFilmById(int filmId) {
        if (films.containsKey(filmId)) {
            return films.get(filmId);
        } else {
            log.error("get film by id error: film with id {} not exists.", filmId);
            throw new FilmNotFoundException(String.format("Film with id: %s was not found!", filmId));
        }
    }

    private void filmValidation(Film film) {
        if (Objects.isNull(film.getName()) || film.getName().isBlank()) {
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
