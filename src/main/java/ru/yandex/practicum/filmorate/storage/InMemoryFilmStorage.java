package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.FilmNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

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
        film.setId(generateId());
        films.put(film.getId(), film);
        log.info("new film was added {}", film);

        return film;
    }

    @Override
    public Film update(Film film) {
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
    public Optional<Film> getFilmById(int filmId) {
        if (films.containsKey(filmId)) {
            return Optional.ofNullable(films.get(filmId));
        } else {
            log.error("get film by id error: film with id {} not exists.", filmId);
            throw new FilmNotFoundException(String.format("Film with id: %s was not found!", filmId));
        }
    }
}
