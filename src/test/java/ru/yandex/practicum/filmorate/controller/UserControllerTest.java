package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.exceptions.UserNotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class UserControllerTest {

    private User user;
    @Autowired
    private UserController controller;

    @BeforeEach
    public void init() {
        user = User.builder()
                .id(1)
                .name("John Smith")
                .login("login")
                .email("example@email.com")
                .birthday(LocalDate.of(1913, 5, 17))
                .build();
    }

    @Test
    @DisplayName("Тестирование валидации почты пользователя")
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
    @DisplayName("Тестирование валидации логина пользователя")
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
    @DisplayName("Тестирование валидации пустого имени пользователя")
    public void validateUserNameBlank() {
        user.setName("            ");
        controller.createUser(user);

        assertEquals(user.getLogin(), user.getName());
    }

    @Test
    @DisplayName("Тестирование валидации имени пользователя null")
    public void validateUserNameNull() {
        user.setName(null);
        controller.createUser(user);

        assertEquals(user.getLogin(), user.getName());
    }

    @Test
    @DisplayName("Тестирование валидации даты рождения пользователя")
    public void validateUserBirthday() {
        user.setBirthday(LocalDate.MAX);
        ValidationException ex = assertThrows(ValidationException.class, () -> controller.createUser(user));

        assertEquals("User birthday cannot be in the future", ex.getMessage());

        user.setBirthday(LocalDate.now());
        controller.createUser(user);

        assertEquals(controller.getUsers().size(), 1);
    }

    @Test
    @DisplayName("Тестирование обновления данных пользователя")
    public void updateUser() {
        controller.createUser(user);

        assertEquals(controller.getUsers().size(), 1);
        assertEquals(user.getId(), 1);

        user.setId(Integer.MIN_VALUE);
        UserNotFoundException ex = assertThrows(UserNotFoundException.class, () -> controller.updateUser(user));

        assertEquals(String.format("User with id: %s was not found!", user.getId()), ex.getMessage());
    }
}