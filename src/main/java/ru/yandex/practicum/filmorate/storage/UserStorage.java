package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Optional;

public interface UserStorage {
    List<User> getUsers();

    User create(User user);

    User update(User user);

    Optional<User> getUserById(int userId);

    List<User> getFriends(int userId);

    List<User> getCommonFriends(int userId, int otherId);

    void addFriend(int userId, int friendId);

    void removeFromFriends(int userId, int friendId);
}
