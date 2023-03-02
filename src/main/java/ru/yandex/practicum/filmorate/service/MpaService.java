package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.MpaNotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.MpaDbStorage;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MpaService {

    private final MpaDbStorage mpaDbStorage;

    public List<Mpa> getMpas() {
        return mpaDbStorage.getMpas();
    }

    public Optional<Mpa> getMpaById(int mpaId) {

        Optional<Mpa> mpaById = mpaDbStorage.getMpaById(mpaId);
        if (mpaById.isEmpty()) {
            log.info("mpa wasn't found by id: mpa with id {} was not found.", mpaId);
            throw new MpaNotFoundException(String.format("Mpa with id: %s was not found!", mpaById));
        }
        return mpaById;
    }
}
