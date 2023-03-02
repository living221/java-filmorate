package ru.yandex.practicum.filmorate.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class MpaDbStorage implements MpaStorage {

    private final Logger log = LoggerFactory.getLogger(MpaDbStorage.class);
    private final JdbcTemplate jdbcTemplate;

    public MpaDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }


    @Override
    public List<Mpa> getMpas() {
        SqlRowSet mpaRows = jdbcTemplate.queryForRowSet("select * from mpas order by id");
        List<Mpa> mpas = new ArrayList<>();
        while (mpaRows.next()) {
            Mpa mpa = new Mpa(
                    mpaRows.getInt("id"),
                    mpaRows.getString("name")
            );
            log.info("Найден mpa рейтинг: {} {}", mpa.getId(), mpa.getName());
            mpas.add(mpa);
        }
        return mpas;
    }

    @Override
    public Optional<Mpa> getMpaById(int id) {

        SqlRowSet mpaRows = jdbcTemplate.queryForRowSet("select * from mpas where id = ?", id);
        if (mpaRows.next()) {
            Mpa mpa = new Mpa(
                    mpaRows.getInt("id"),
                    mpaRows.getString("name")
            );
            log.info("Найден mpa рейтинг: {} {}", mpa.getId(), mpa.getName());
            return Optional.of(mpa);
        } else {
            log.info("Mpa рейтинг с идентификатором {} не найден.", id);
            return Optional.empty();
        }
    }
}
