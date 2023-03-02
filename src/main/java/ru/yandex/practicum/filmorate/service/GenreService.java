package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.GenreNotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.GenreDbStorage;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class GenreService {

    private final GenreDbStorage genreDbStorage;

    public List<Genre> getGenres() {
        return genreDbStorage.getGenres();
    }

    public Optional<Genre> getGenreById(int genreId) {
        Optional<Genre> genreById = genreDbStorage.getGenreById(genreId);
        if (genreById.isEmpty()) {
            log.info("genre wasn't found by id: genre with id {} was not found.", genreId);
            throw new GenreNotFoundException(String.format("Genre with id: %s was not found!", genreId));
        }
        return genreById;
    }
}
