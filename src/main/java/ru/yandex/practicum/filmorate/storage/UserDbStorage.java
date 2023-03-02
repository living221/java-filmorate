package ru.yandex.practicum.filmorate.storage;

import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Optional;

@Component
@Primary
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbcTemplate;

    public UserDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<User> getUsers() {
        String sql = "SELECT * FROM USERS";

        return jdbcTemplate.query(sql, new UserMapper());
    }

    @Override
    public User create(User user) {

        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("users")
                .usingGeneratedKeyColumns("id");

        int userId = simpleJdbcInsert.executeAndReturnKey(user.toMap()).intValue();
        user.setId(userId);
        return user;
    }

    @Override
    public User update(User user) {
        String sql = "update users set email = ?, login = ?, name = ?, birthday = ? where id = ?";

        int update = jdbcTemplate.update(sql,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday(),
                user.getId());

        if (update > 0) {
            return user;
        } else {
            return null;
        }
    }

    @Override
    public Optional<User> getUserById(int userId) {

        String sql = "SELECT * FROM USERS WHERE ID = ?";
        return Optional.ofNullable(jdbcTemplate.queryForObject(sql, new UserMapper(), userId));
    }

    @Override
    public List<User> getFriends(int userId) {

        String sql = "select * from users " +
                "where id in (select f.friend_id from friendship f join users u on u.id = f.user_id where u.id = ?)";

        return jdbcTemplate.query(sql, new UserMapper(), userId);
    }

    @Override
    public List<User> getCommonFriends(int userId, int otherId) {
        String sql = "select * from users where id in " +
                "(select f.friend_id from friendship f JOIN users u on u.id = f.user_id where u.id = ?)" +
                " and id in (select f.friend_id FROM friendship f JOIN users u on u.id = f.user_id where u.id = ?)";

        return jdbcTemplate.query(sql, new UserMapper(), userId, otherId);
    }

    @Override
    public void addFriend(int userId, int friendId) {
        final String sql = "insert into friendship (user_id, friend_id) values (?, ?)";

        jdbcTemplate.update(sql, userId, friendId);
    }

    @Override
    public void removeFromFriends(int userId, int friendId) {
        final String sql = "delete from friendship where user_id = ? and friend_id = ?";

        jdbcTemplate.update(sql, userId, friendId);
    }
}
