package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exceptions.UserNotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public List<Film> getFilms() {
        return filmStorage.getFilms();
    }

    public Film createFilm(Film film) {
        filmValidation(film);
        return filmStorage.create(film);
    }

    public Film updateFilm(Film film) {
        filmValidation(film);
        if (filmNotExists(film.getId())) {
            log.error("film service update film error: film with id {} was not found.", film.getId());
            throw new FilmNotFoundException(String.format("Film with id: %s was not found!", film.getId()));
        }
        return filmStorage.update(film);
    }

    public Optional<Film> getFilmById(int filmId) {
        if (filmNotExists(filmId)) {
            log.error("film service get film by id error: film with id {} was not found.", filmId);
            throw new FilmNotFoundException(String.format("Film with id: %s was not found!", filmId));
        }
        return filmStorage.getFilmById(filmId);
    }

    public void addLike(int filmId, int userId) {
        validateFilmAndUser(filmId, userId);

        if (filmStorage.getFilms().stream().anyMatch(f -> f.getId() == filmId)) {
            Optional<Film> filmById = filmStorage.getFilmById(filmId);
            if (filmById.isPresent()) {
                if (filmById.get().getLikes().contains(userId)) {
                    log.error("film service add like to film error: " +
                            "user with id {} already add like to film with id {}.", userId, filmId);
                    throw new ValidationException(String.format("user with id %s already add like " +
                            "to film with id %s.", userId, filmId));
                }
            }
        }

        for (Film film : filmStorage.getFilms()) {
            if (filmId == film.getId()) {
                filmStorage.addLike(filmId, userId);
            }
        }
    }

    public void removeLike(int filmId, int userId) {
        validateFilmAndUser(filmId, userId);

        for (Film film : filmStorage.getFilms()) {
            if (filmId == film.getId()) {
                if (!(film.getLikes().contains(userId))) {
                    log.error("film service remove like from film error: " +
                            "can't find likes from user with id {} in film with id {} likes.", userId, filmId);
                    throw new ValidationException(String.format("no likes from user with id %s found for " +
                            "film with id %s.", userId, filmId));
                } else {
                    filmStorage.removeLike(filmId, userId);
                }
            }
        }
    }


    public List<Film> getPopularFilms(int count) {

        if (count <= 0) {
            log.error("film service get popular films error: " +
                    "film count can't be negative, count: {}.", count);
            throw new ValidationException(String.format("film count can't be negative, count: %s.", count));
        }

        if (count > filmStorage.getFilms().size()) {
            count = filmStorage.getFilms().size();
        }


        return filmStorage.getFilms()
                .stream()
                .sorted((o1, o2) -> o2.getLikes().size() - o1.getLikes().size())
                .limit(count)
                .collect(Collectors.toList());
    }

    public boolean userExists(int userId) {
        for (User user : userStorage.getUsers()) {
            if (userId == user.getId()) {
                return true;
            }
        }
        return false;
    }

    public boolean filmNotExists(int filmId) {
        for (Film film : filmStorage.getFilms()) {
            if (filmId == film.getId()) {
                return false;
            }
        }
        return true;
    }

    private void validateFilmAndUser(int filmId, int userId) {
        if (filmNotExists(filmId)) {
            log.error("film service add like to film error: film with id {} was not found.", filmId);
            throw new FilmNotFoundException(String.format("Film with id: %s was not found!", filmId));
        }
        if (!userExists(userId)) {
            log.error("film service add like to film error: user with id {} was not found.", userId);
            throw new UserNotFoundException(String.format("User with id: %s was not found!", userId));
        }
    }

    private void filmValidation(Film film) {
        if (Objects.isNull(film.getName()) || film.getName().isBlank()) {
            log.error("film validation error: film with name {} was attempted to add.", film.getName());
            throw new ValidationException("Film name cannot be empty!");
        }
        if (film.getDescription().length() > 200) {
            log.error("film validation error: film with description {} was attempted to add.", film.getDescription());
            throw new ValidationException("Film description length cannot be more than 200 signs.");
        }
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            log.error("film validation error: film with release date {} was attempted to add.", film.getReleaseDate());
            throw new ValidationException("Film release date cannot be before 28.12.1895");
        }
        if (film.getDuration() <= 0) {
            log.error("film validation error: film with duration {} was attempted to add.", film.getDuration());
            throw new ValidationException("Film duration cannot be negative or zero.");
        }
    }
}
