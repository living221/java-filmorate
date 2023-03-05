package ru.yandex.practicum.filmorate.storage.mpa;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;

@Component
public class MpaDbStorage implements MpaStorage {

    private final Logger log = LoggerFactory.getLogger(MpaDbStorage.class);
    private final JdbcTemplate jdbcTemplate;
    private final MpaMapper mpaMapper;

    public MpaDbStorage(JdbcTemplate jdbcTemplate, MpaMapper mpaMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.mpaMapper = mpaMapper;
    }


    @Override
    public List<Mpa> getMpas() {
        String sql = "SELECT m.id, m.name FROM mpas m ORDER BY id";

        return jdbcTemplate.query(sql, mpaMapper);
    }

    @Override
    public Mpa getMpaById(int id) {
        SqlRowSet mpaRows = jdbcTemplate.queryForRowSet("SELECT m.id, m.name FROM mpas m WHERE id = ?", id);
        if (mpaRows.next()) {
            Mpa mpa = new Mpa(
                    mpaRows.getInt("id"),
                    mpaRows.getString("name")
            );
            log.info("MPA found: {} {}", mpa.getId(), mpa.getName());
            return mpa;
        } else {
            log.info("MPA with id: {} not found.", id);
            return null;
        }
    }
}
