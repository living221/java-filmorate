package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class UserDbStorageTest {

    private final UserDbStorage userDbStorage;

    private User user;
    private User friend;
    private User commonFriend;

    @BeforeEach
    public void init() {
        user = User.builder()
                .id(1)
                .name("user name")
                .login("user login")
                .email("user@mail.com")
                .birthday(LocalDate.of(2010, 7, 19))
                .build();

        friend = User.builder()
                .id(2)
                .name("friend name")
                .login("friend login")
                .email("friend@mail.com")
                .birthday(LocalDate.of(2005, 3, 23))
                .build();

        commonFriend = User.builder()
                .id(3)
                .name("common friend name")
                .login("common friend login")
                .email("commonfriend@mail.com")
                .birthday(LocalDate.of(2000, 1, 11))
                .build();
    }

    @Test
    @DisplayName("Тестирование получения всех пользователей")
    public void testGetUsers() {
        userDbStorage.create(user);
        List<User> users = userDbStorage.getUsers();

        assertEquals(users.size(), 1);
    }

    @Test
    @DisplayName("Тестирование добавления пользователя")
    public void create() {
        User addedUser = userDbStorage.create(user);

        assertEquals(addedUser.getId(), 1);
        assertEquals(addedUser.getName(), "user name");
        assertEquals(addedUser.getLogin(), "user login");
        assertEquals(addedUser.getEmail(), "user@mail.com");
        assertEquals(addedUser.getBirthday(), LocalDate.of(2010, 7, 19));
    }

    @Test
    @DisplayName("Тестирование обновления данных пользователя")
    public void testUserUpdate() {
        User addedUser = userDbStorage.create(user);
        user.setName("updated name");

        userDbStorage.update(user);
        Optional<User> updatedUser = userDbStorage.getUserById(addedUser.getId());

        assertThat(updatedUser)
                .isPresent()
                .hasValueSatisfying(user ->
                        assertThat(user).hasFieldOrPropertyWithValue("name", "updated name")
                );
    }

    @Test
    @DisplayName("Тестирование поиска пользователя по id")
    public void testGetUserById() {
        User addedUser = userDbStorage.create(user);

        Optional<User> userFormDb = userDbStorage.getUserById(addedUser.getId());

        assertThat(userFormDb)
                .isPresent()
                .hasValueSatisfying(user ->
                        assertThat(user).hasFieldOrPropertyWithValue("id", addedUser.getId())
                );
    }

    @Test
    @DisplayName("Тестирование получения друзей пользователя")
    public void testGetUserFriends() {
        User addedUser = userDbStorage.create(user);
        User addedFriend = userDbStorage.create(friend);

        List<User> noFriends = userDbStorage.getFriends(addedFriend.getId());

        assertEquals(noFriends.size(), 0);

        userDbStorage.addFriend(addedUser.getId(), addedFriend.getId());
        List<User> friends = userDbStorage.getFriends(addedUser.getId());

        assertEquals(friends.size(), 1);
    }

    @Test
    @DisplayName("Тестирование получения списка общих друзей")
    public void testGetCommonFriends() {
        User addedUser = userDbStorage.create(user);
        User addedFriend = userDbStorage.create(friend);
        User addedCommonFriend = userDbStorage.create(commonFriend);

        userDbStorage.addFriend(addedUser.getId(), addedCommonFriend.getId());
        userDbStorage.addFriend(addedFriend.getId(), addedCommonFriend.getId());

        List<User> commonFriends = userDbStorage.getCommonFriends(addedUser.getId(), addedFriend.getId());

        assertEquals(commonFriends.size(), 1);
    }

    @Test
    @DisplayName("Тестирование добавления друга")
    public void testAddFriend() {
        User addedUser = userDbStorage.create(user);
        User addedFriend = userDbStorage.create(friend);

        userDbStorage.addFriend(addedUser.getId(), addedFriend.getId());
        List<User> noFriends = userDbStorage.getFriends(addedFriend.getId());

        assertEquals(noFriends.size(), 0);

        List<User> friends = userDbStorage.getFriends(addedUser.getId());

        assertEquals(friends.size(), 1);
    }

    @Test
    @DisplayName("Тестирование удаления из друзей")
    public void removeFromFriends() {
        User addedUser = userDbStorage.create(user);
        User addedFriend = userDbStorage.create(friend);

        userDbStorage.addFriend(addedUser.getId(), addedFriend.getId());
        List<User> friends = userDbStorage.getFriends(addedUser.getId());

        assertEquals(friends.size(), 1);

        userDbStorage.removeFromFriends(addedUser.getId(), addedFriend.getId());
        List<User> noFriends = userDbStorage.getFriends(addedFriend.getId());

        assertEquals(noFriends.size(), 0);
    }
}