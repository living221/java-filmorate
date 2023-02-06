package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.UserNotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        userValidation(user);

        user.setId(generateId());
        users.put(user.getId(), user);
        log.info("new user was added {}", user);

        return user;
    }

    @Override
    public User update(User user) {
        userValidation(user);

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
    public User getUserById(int userId) {
        if (users.containsKey(userId)) {
            return users.get(userId);
        } else {
            log.debug("get user by id error: user with id {} not exists.", userId);
            throw new UserNotFoundException(String.format("User with id: %s was not found!", userId));
        }
    }

    private int generateId() {
        generatorId++;
        return generatorId;
    }

    private void userValidation(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            log.debug("user validation error: user with email {} was attempted to create.", user.getEmail());
            throw new ValidationException("User email cannot be empty.");
        }
        if (!user.getEmail().contains("@")) {
            log.debug("user validation error: user with email {} was attempted to create.", user.getEmail());
            throw new ValidationException("User email have to contain '@' sign.");
        }
        if (user.getLogin() == null || user.getLogin().isBlank()) {
            log.debug("user validation error: user with login {} was attempted to create.", user.getLogin());
            throw new ValidationException("User login cannot be empty or blank.");
        }
        if (user.getLogin().contains(" ")) {
            log.debug("user validation error: user with login {} was attempted to create.", user.getLogin());
            throw new ValidationException("User login cannot have spaces in it.");
        }
        if (user.getBirthday().isAfter(LocalDate.now())) {
            log.debug("user validation error: user with birthday {} was attempted to create.", user.getBirthday());
            throw new ValidationException("User birthday cannot be in the future");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }
}
