package ru.yandex.practicum.filmorate.storage.genre;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class GenreStorageTest {

    private final GenreDbStorage genreDbStorage;

    @Test
    @DisplayName("Тестирование поиска всех жанров")
    public void testGetGenres() {
        List<Genre> genres = genreDbStorage.getGenres();

        assertEquals(genres.size(), 6);
    }

    @Test
    @DisplayName("Тестирование поиска жанра по id")
    public void getGenreById() {
        Genre genre = genreDbStorage.getGenreById(1);

        assertEquals(genre.getId(), 1);

        genre = genreDbStorage.getGenreById(9999);

        assertNull(genre);
    }
}