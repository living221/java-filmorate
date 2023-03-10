package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {

    private final Map<Integer, User> users = new HashMap<>();
    private int generatorId = 0;

    @Override
    public List<User> getUsers() {
        return new ArrayList<>(users.values());
    }

    @Override
    public User create(User user) {
        user.setId(generateId());
        users.put(user.getId(), user);
        log.info("new user was added {}", user);

        return user;
    }

    @Override
    public User update(User user) {
        if (users.containsKey(user.getId())) {
            users.put(user.getId(), user);

            log.info("user with id {} was updated. user: {}", user.getId(), user);
            return user;
        } else {
            log.debug("user update error: user with id {} was attempted to update.", user.getId());
            throw new UserNotFoundException(String.format("User with id: %s was not found!", user.getId()));
        }
    }

    @Override
    public Optional<User> getUserById(int userId) {
        if (users.containsKey(userId)) {
            return Optional.ofNullable(users.get(userId));
        } else {
            log.debug("get user by id error: user with id {} not exists.", userId);
            throw new UserNotFoundException(String.format("User with id: %s was not found!", userId));
        }
    }

    @Override
    public List<User> getFriends(int userId) {
        return getUsers()
                .stream()
                .filter(u -> u.getFriends().contains(userId))
                .collect(Collectors.toList());
    }

    @Override
    public List<User> getCommonFriends(int userId, int otherId) {
        ArrayList<User> commonFriends = new ArrayList<>();

        Optional<User> user = getUserById(userId);
        Set<Integer> friendsOfUser = new HashSet<>();
        if (user.isPresent()) {
            friendsOfUser = user.get().getFriends();
        }

        Optional<User> otherUser = getUserById(otherId);
        Set<Integer> friendsOfOtherUser = new HashSet<>();
        if (otherUser.isPresent()) {
            friendsOfOtherUser = otherUser.get().getFriends();
        }

        for (Integer commonId : friendsOfUser) {
            if (friendsOfOtherUser.contains(commonId)) {
                Optional<User> userById = getUserById(commonId);
                userById.ifPresent(commonFriends::add);
            }
        }
        return commonFriends;
    }

    @Override
    public void addFriend(int userId, int friendId) {
        for (User user : getUsers()) {
            if (userId == user.getId()) {
                user.getFriends().add(friendId);
            }
        }

        for (User user : getUsers()) {
            if (friendId == user.getId()) {
                user.getFriends().add(userId);
            }
        }
    }

    @Override
    public void removeFromFriends(int userId, int friendId) {
        for (User user : getUsers()) {
            if (userId == user.getId()) {
                user.getFriends().remove(userId);
            }
        }

        for (User user : getUsers()) {
            if (friendId == user.getId()) {
                user.getFriends().remove(friendId);
            }
        }
    }

    private int generateId() {
        return ++generatorId;
    }
}
