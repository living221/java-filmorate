package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class UserControllerTest {

    private User user;
    @Autowired
    private UserController controller;

    @BeforeEach
    public void init() {
        user = new User();
        user.setId(1);
        user.setName("John Smith");
        user.setLogin("login");
        user.setEmail("example@email.com");
        user.setBirthday(LocalDate.of(1913, 5, 17));
    }

    @Test
    public void validateUserEmail() {
        user.setEmail("            ");
        ValidationException ex = assertThrows(ValidationException.class, () -> controller.createUser(user));

        assertEquals("User email cannot be empty.", ex.getMessage());

        user.setEmail("");
        ex = assertThrows(ValidationException.class, () -> controller.createUser(user));

        assertEquals("User email cannot be empty.", ex.getMessage());

        user.setEmail(null);
        ex = assertThrows(ValidationException.class, () -> controller.createUser(user));

        assertEquals("User email cannot be empty.", ex.getMessage());

        user.setEmail("abcdefghijklmnop");
        ex = assertThrows(ValidationException.class, () -> controller.createUser(user));

        assertEquals("User email have to contain '@' sign.", ex.getMessage());
    }

    @Test
    public void validateUserLogin() {
        user.setLogin("            ");
        ValidationException ex = assertThrows(ValidationException.class, () -> controller.createUser(user));

        assertEquals("User login cannot be empty or blank.", ex.getMessage());

        user.setLogin("");
        ex = assertThrows(ValidationException.class, () -> controller.createUser(user));

        assertEquals("User login cannot be empty or blank.", ex.getMessage());

        user.setLogin(null);
        ex = assertThrows(ValidationException.class, () -> controller.createUser(user));

        assertEquals("User login cannot be empty or blank.", ex.getMessage());

        user.setLogin("login with spaces");
        ex = assertThrows(ValidationException.class, () -> controller.createUser(user));

        assertEquals("User login cannot have spaces in it.", ex.getMessage());
    }

    @Test
    public void validateUserNameBlank() {
        user.setName("            ");
        controller.createUser(user);

        assertEquals(user.getLogin(), user.getName());
    }

    @Test
    public void validateUserNameNull() {
        user.setName(null);
        controller.createUser(user);

        assertEquals(user.getLogin(), user.getName());
    }

    @Test
    public void validateUserBirthday() {
        user.setBirthday(LocalDate.MAX);
        ValidationException ex = assertThrows(ValidationException.class, () -> controller.createUser(user));

        assertEquals("User birthday cannot be in the future", ex.getMessage());

        user.setBirthday(LocalDate.now());
        controller.createUser(user);

        assertEquals(controller.users.values().size(), 1);
    }

    @Test
    public void updateUser() {
        controller.createUser(user);

        assertEquals(controller.users.values().size(), 1);
        assertEquals(user.getId(), 1);

        user.setId(Integer.MIN_VALUE);
        ValidationException ex = assertThrows(ValidationException.class, () -> controller.updateUser(user));

        assertEquals(String.format("User with id: %s was not found!", user.getId()), ex.getMessage());
    }
}