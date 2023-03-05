package ru.yandex.practicum.filmorate.storage.user;

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
    private final UserMapper userMapper;

    public UserDbStorage(JdbcTemplate jdbcTemplate, UserMapper userMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.userMapper = userMapper;
    }

    @Override
    public List<User> getUsers() {
        String sql = "SELECT u.id, u.email, u.login, u.name, u.birthday FROM users u";

        return jdbcTemplate.query(sql, userMapper);
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
        String sql = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ?";

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

        String sql = "SELECT u.id, u.email, u.login, u.name, u.birthday FROM users u WHERE id = ?";
        return Optional.ofNullable(jdbcTemplate.queryForObject(sql, userMapper, userId));
    }

    @Override
    public List<User> getFriends(int userId) {

        String sql = "SELECT u.id, u.email, u.login, u.name, u.birthday FROM users u " +
                "WHERE id IN (SELECT f.friend_id FROM friendship f JOIN users u ON u.id = f.user_id WHERE u.id = ?)";

        return jdbcTemplate.query(sql, userMapper, userId);
    }

    @Override
    public List<User> getCommonFriends(int userId, int otherId) {
        String sql = "SELECT u.id, u.email, u.login, u.name, u.birthday FROM users u WHERE id IN " +
                "(SELECT f.friend_id FROM friendship f JOIN users u ON u.id = f.user_id WHERE u.id = ?)" +
                " AND id IN (SELECT f.friend_id FROM friendship f JOIN users u ON u.id = f.user_id WHERE u.id = ?)";

        return jdbcTemplate.query(sql, userMapper, userId, otherId);
    }

    @Override
    public void addFriend(int userId, int friendId) {
        final String sql = "INSERT INTO friendship (user_id, friend_id) VALUES (?, ?)";

        jdbcTemplate.update(sql, userId, friendId);
    }

    @Override
    public void removeFromFriends(int userId, int friendId) {
        final String sql = "DELETE FROM friendship WHERE user_id = ? AND friend_id = ?";

        jdbcTemplate.update(sql, userId, friendId);
    }
}
