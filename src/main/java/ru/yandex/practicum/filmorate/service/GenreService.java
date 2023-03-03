package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.GenreNotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class GenreService {

    private final GenreDbStorage genreDbStorage;

    public List<Genre> getGenres() {
        return genreDbStorage.getGenres();
    }

    public Genre getGenreById(int genreId) {
        Genre genreById = genreDbStorage.getGenreById(genreId);

        if (Objects.isNull(genreById)) {
            log.info("genre wasn't found by id: genre with id {} was not found.", genreId);
            throw new GenreNotFoundException(String.format("Genre with id: %s was not found!", genreId));
        }
        return genreById;
    }
}
