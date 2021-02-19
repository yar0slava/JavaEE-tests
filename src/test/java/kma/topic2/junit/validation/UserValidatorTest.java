package kma.topic2.junit.validation;

import kma.topic2.junit.exceptions.ConstraintViolationException;
import kma.topic2.junit.exceptions.LoginExistsException;
import kma.topic2.junit.model.NewUser;
import kma.topic2.junit.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserValidatorTest {

    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private UserValidator userValidator;

    @Test
    void shouldPassValidation() {
        userValidator.validateNewUser(
                NewUser.builder()
                        .fullName("Test")
                        .login("TestLogin")
                        .password("pass")
                        .build()
        );

        verify(userRepository).isLoginExists("TestLogin");
    }

    @Test
    void shouldThrowExceptionIfLoginExists() {

        when(userRepository.isLoginExists("TestLogin")).thenReturn(true);

        assertThatThrownBy(() -> userValidator.validateNewUser(
                NewUser.builder()
                        .fullName("Test")
                        .login("TestLogin")
                        .password("pass")
                        .build()
        ))
                .isInstanceOf(LoginExistsException.class);
    }

    @ParameterizedTest
    @MethodSource("testPasswordDataProwider")
    void shouldThrowExceptionIfPasswordInvalid(String password, List<String> errors) {

        assertThatThrownBy(() -> userValidator.validateNewUser(
                NewUser.builder()
                        .fullName("Test")
                        .login("TestLogin")
                        .password(password)
                        .build()
        ))
                .isInstanceOfSatisfying(ConstraintViolationException.class,
                        ex -> assertThat(ex.getErrors()).isEqualTo(errors));

    }

    private static Stream<Arguments> testPasswordDataProwider(){
        return Stream.of(
                Arguments.of("pa",List.of("Password has invalid size")),
                Arguments.of("password",List.of("Password has invalid size")),
                Arguments.of("password@#$",List.of("Password has invalid size","Password doesn't match regex")),
                Arguments.of("p@",List.of("Password has invalid size","Password doesn't match regex"))
        );
    }
}