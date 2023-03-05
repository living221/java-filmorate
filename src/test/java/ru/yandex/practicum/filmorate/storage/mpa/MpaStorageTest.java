package ru.yandex.practicum.filmorate.storage.mpa;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class MpaStorageTest {

    private final MpaDbStorage mpaDbStorage;

    @Test
    @DisplayName("Тестирование поиска MPA по id")
    public void testFindMpaById() {
        Mpa mpa = mpaDbStorage.getMpaById(1);

        assertEquals(mpa.getId(), 1);

        mpa = mpaDbStorage.getMpaById(9999);

        assertNull(mpa);
    }

    @Test
    @DisplayName("Тестирование поиска всех MPA")
    public void testGetMpas() {
        List<Mpa> mpas = mpaDbStorage.getMpas();

        assertEquals(mpas.size(), 5);
    }
}
