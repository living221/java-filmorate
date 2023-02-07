package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exceptions.UserNotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;
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
        return filmStorage.create(film);
    }

    public Film updateFilm(Film film) {
        return filmStorage.update(film);
    }

    public Film getFilmById(int filmId) {
        if (!filmExists(filmId)) {
            log.debug("film service get film by id error: film with id {} was not found.", filmId);
            throw new FilmNotFoundException(String.format("Film with id: %s was not found!", filmId));
        }
        return filmStorage.getFilmById(filmId);
    }

    public void addLike(int filmId, int userId) {
        validateFilmAndUser(filmId, userId);

        if (filmStorage.getFilms().stream().anyMatch(f -> f.getId() == filmId)){
            if (filmStorage.getFilmById(filmId).getLikes().contains(userId)) {
                log.debug("film service add like to film error: " +
                                "user with id {} already add like to film with id {}.", userId, filmId);
                        throw new ValidationException(String.format("user with id %s already add like " +
                                "to film with id %s.", userId, filmId));
            }
        }

        for (Film film : filmStorage.getFilms()) {
            if (filmId == film.getId()) {
                film.getLikes().add(userId);
            }
        }
    }

    public void removeLike(int filmId, int userId) {
        validateFilmAndUser(filmId, userId);

        for (Film film : filmStorage.getFilms()) {
            if (filmId == film.getId()) {
                if (!(film.getLikes().contains(userId))) {
                    log.debug("film service remove like from film error: " +
                            "can't find likes from user with id {} in film with id {} likes.", userId, filmId);
                    throw new ValidationException(String.format("no likes from user with id %s found for " +
                            "film with id %s.", userId, filmId));
                } else {
                    film.getLikes().remove(userId);
                }
            }
        }
    }


    public List<Film> getPopularFilms(int count) {

        if (count <= 0) {
            log.debug("film service get popular films error: " +
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

    public boolean filmExists(int filmId) {
        for (Film film : filmStorage.getFilms()) {
            if (filmId == film.getId()) {
                return true;
            }
        }
        return false;
    }

    private void validateFilmAndUser(int filmId, int userId) {
        if (!filmExists(filmId)) {
            log.debug("film service add like to film error: film with id {} was not found.", filmId);
            throw new FilmNotFoundException(String.format("Film with id: %s was not found!", filmId));
        }
        if (!userExists(userId)) {
            log.debug("film service add like to film error: user with id {} was not found.", userId);
            throw new UserNotFoundException(String.format("User with id: %s was not found!", userId));
        }
    }
}
