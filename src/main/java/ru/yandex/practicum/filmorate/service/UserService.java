package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.UserNotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

        for (User user : userStorage.getUsers()) {
            if (userId == user.getId()) {
                user.getFriends().add(friendId);
            }
        }

        for (User user : userStorage.getUsers()) {
            if (friendId == user.getId()) {
                user.getFriends().add(userId);
            }
        }
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

        for (User user : userStorage.getUsers()) {
            if (userId == user.getId()) {
                user.getFriends().remove(userId);
            }
        }

        for (User user : userStorage.getUsers()) {
            if (friendId == user.getId()) {
                user.getFriends().remove(friendId);
            }
        }
    }

    public List<User> getFriends(int userId) {

        if (!userExists(userId)) {
            log.debug("user service get friend list error: user with id {} was not found.", userId);
            throw new UserNotFoundException(String.format("User with id: %s was not found!", userId));
        }

        return userStorage
                .getUsers()
                .stream()
                .filter(u -> u.getFriends().contains(userId))
                .collect(Collectors.toList());
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
        ArrayList<User> commonFriends = new ArrayList<>();

        Set<Integer> friendsOfUser = userStorage.getUserById(userId).getFriends();

        Set<Integer> friendsOfOtherUser = userStorage.getUserById(otherId).getFriends();

        for (Integer commonId : friendsOfUser) {
            if (friendsOfOtherUser.contains(commonId)) {
                commonFriends.add(userStorage.getUserById(commonId));
            }
        }

        return commonFriends;
    }

    public User getUserById(int userId) {
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
