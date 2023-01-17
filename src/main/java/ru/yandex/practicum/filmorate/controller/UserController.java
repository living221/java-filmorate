package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    private int generatorId = 0;

    protected final HashMap<Integer, User> users = new HashMap<>();

    @GetMapping
    public List<User> findAll() {

        return new ArrayList<>(users.values());
    }

    @PostMapping
    public User createUser(@Valid @RequestBody User user) {
        userValidation(user);

        user.setId(generateId());
        users.put(user.getId(), user);
        log.info("new user was added {}", user);

        return user;
    }

    @PutMapping
    public User updateUser(@Valid @RequestBody User user) {
        userValidation(user);

        if (users.containsKey(user.getId())) {
            users.put(user.getId(), user);

            log.info("user with id {} was updated. user: {}", user.getId(), user);
            return user;
        } else {
            log.debug("user update error: user with id {} was attempted to update.", user.getId());
            throw new ValidationException(String.format("User with id: %s was not found!", user.getId()));
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
