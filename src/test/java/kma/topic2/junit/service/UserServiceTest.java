package kma.topic2.junit.service;

import kma.topic2.junit.exceptions.UserNotFoundException;
import kma.topic2.junit.model.NewUser;
import kma.topic2.junit.model.User;
import kma.topic2.junit.repository.UserRepository;
import kma.topic2.junit.validation.UserValidator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@SpringBootTest
class UserServiceTest {

    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;
    @SpyBean
    private UserValidator userValidator;

    @Test
    void shouldCreateUser() {
        userService.createNewUser(
                NewUser.builder()
                        .fullName("Test")
                        .login("TestLogin")
                        .password("pass")
                        .build()
        );

        assertThat(userRepository.getUserByLogin("TestLogin"))
                .returns("pass", User::getPassword)
                .returns("TestLogin", User::getLogin);

        verify(userValidator).validateNewUser(NewUser.builder()
                .fullName("Test")
                .login("TestLogin")
                .password("pass")
                .build());

        verify(userValidator).validateNewUser(any());
    }

    @Test
    void shouldGetUser() {
        userRepository.saveNewUser(
                NewUser.builder()
                        .fullName("Test")
                        .login("TestLogin")
                        .password("pass")
                        .build()
        );

        assertThat(userService.getUserByLogin("TestLogin"))
                .returns("pass", User::getPassword)
                .returns("TestLogin", User::getLogin);
    }

    @Test
    void shouldThrowUserNotFound() {

        assertThatThrownBy(() -> userService.getUserByLogin("NonExistingLogin"))
                .isInstanceOfSatisfying(UserNotFoundException.class,
                        ex -> assertThat(ex.getMessage()).isEqualTo("Can't find user by login: " + "NonExistingLogin"));
    }
}