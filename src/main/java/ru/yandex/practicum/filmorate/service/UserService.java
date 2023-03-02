package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.UserNotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserStorage userStorage;

    public List<User> getUsers() {
        return userStorage.getUsers();
    }

    public User createUser(User user) {
        return userStorage.create(user);
    }

    public User updateUser(User user) {
        if (!userExists(user.getId())) {
            log.debug("user service update user error: user with id {} was not found.", user.getId());
            throw new UserNotFoundException(String.format("User with id: %s was not found!", user.getId()));
        }
        return userStorage.update(user);
    }

    public void addToFriends(int userId, int friendId) {
        if (!userExists(userId)) {
            log.debug("user service add to friend list error: user with id {} was not found.", userId);
            throw new UserNotFoundException(String.format("User with id: %s was not found!", userId));
        }

        if (!userExists(friendId)) {
            log.debug("user service add to friend list error: user with id {} was not found.", friendId);
            throw new UserNotFoundException(String.format("User with id: %s was not found!", friendId));
        }

        if (userId == friendId) {
            log.debug("trying to add users with the same id to their friends list: id {}", userId);
            throw new ValidationException(String.format("Users with the same cannot be friends, id: %s", userId));
        }
        for (User friend : userStorage.getFriends(userId)) {
            if (friend.getId() == friendId) {
                log.debug("user with id: {} already have user with id: {} in friend list", userId, friendId);
                throw new ValidationException(String.format("user with id: %s already have user with id: %s " +
                        "in friend list", userId, friendId));
            }
        }
        userStorage.addFriend(userId, friendId);
    }

    public void removeFromFriends(int userId, int friendId) {
        if (!userExists(userId)) {
            log.debug("user service remove from friend list error: user with id {} was not found.", userId);
            throw new UserNotFoundException(String.format("User with id: %s was not found!", userId));
        }

        if (!userExists(friendId)) {
            log.debug("user service remove from friend list error: user with id {} was not found.", friendId);
            throw new UserNotFoundException(String.format("User with id: %s was not found!", friendId));
        }

        if (userId == friendId) {
            log.debug("trying to remove from friend list users with the same id: id {}", userId);
            throw new UserNotFoundException(String.format("Users with the same id cannot be friends, id: %s", userId));
        }
        userStorage.removeFromFriends(userId, friendId);
    }

    public List<User> getFriends(int userId) {

        if (!userExists(userId)) {
            log.debug("user service get friend list error: user with id {} was not found.", userId);
            throw new UserNotFoundException(String.format("User with id: %s was not found!", userId));
        }

        return userStorage.getFriends(userId);
    }

    public List<User> getCommonFriends(int userId, int otherId) {
        if (!userExists(userId)) {
            log.debug("user service get common friends error: user with id {} was not found.", userId);
            throw new UserNotFoundException(String.format("User with id: %s was not found!", userId));
        }

        if (!userExists(otherId)) {
            log.debug("user service get common friends error: user with id {} was not found.", otherId);
            throw new UserNotFoundException(String.format("User with id: %s was not found!", otherId));
        }

        if (userId == otherId) {
            log.debug("trying to get common friends for users with the same id: id {}", userId);
            throw new ValidationException(String.format("Users with the same id cannot be friends, id: %s", userId));
        }
        return userStorage.getCommonFriends(userId, otherId);
    }

    public Optional<User> getUserById(int userId) {
        if (!userExists(userId)) {
            log.debug("user service get user by id error: user with id {} was not found.", userId);
            throw new UserNotFoundException(String.format("User with id: %s was not found!", userId));
        }

        return userStorage.getUserById(userId);
    }

    public boolean userExists(int userId) {
        for (User user : userStorage.getUsers()) {
            if (userId == user.getId()) {
                return true;
            }
        }
        return false;
    }
}
