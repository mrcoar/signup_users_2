package cl.maraneda.signup.service;

import cl.maraneda.signup.exceptions.UserAlreadyExistsException;
import cl.maraneda.signup.dto.NumberDTO;
import cl.maraneda.signup.dto.UserDTO;
import cl.maraneda.signup.model.User;
import cl.maraneda.signup.repository.UserRepository;
import cl.maraneda.signup.service.impl.NumberService;
import cl.maraneda.signup.service.impl.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.transaction.TransactionSystemException;

import javax.persistence.RollbackException;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.Base64;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.AdditionalMatchers.not;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @InjectMocks
    public transient UserService userService;

    @Mock
    public transient NumberService numberService;

    @Mock
    public transient ModelMapper mapper;

    @Mock
    public transient UserRepository userRepository;

    private transient UserDTO correctUser;

    private static void sleep(long millis){
        try{
            Thread.sleep(millis);
        }catch (InterruptedException e){
            /* Nothing */
        }
    }

    @BeforeEach
    void performBeforeEachTest(){
        correctUser =
            UserDTO.builder()
                   .id(UUID.randomUUID().toString())
                   .name("First User")
                   .email("name@domain.ext")
                   .password(Base64.getEncoder().encodeToString("1A2bcdefg".getBytes()))
                   .build();
    }

    private void prepareTestForCorrectUser(){
        when(mapper.map(correctUser, User.class)).thenAnswer(i ->
                User.builder()
                        .id(correctUser.getId())
                        .name(correctUser.getName())
                        .email(correctUser.getEmail())
                        .password(correctUser.getPassword())
                        .build()
        );
        when(userService.addUser(correctUser)).thenAnswer(i -> {
            Date today = new Date();
            return UserDTO.builder()
                    .id(correctUser.getId())
                    .name(correctUser.getName())
                    .email(correctUser.getEmail())
                    .password(correctUser.getPassword())
                    .created(today)
                    .lastLogin(today)
                    .isActive(Boolean.TRUE)
                    .build();
        });
    }

    @Test
    void testInsertCorrectUserWithPhoneNumbers(){
        prepareTestForCorrectUser();
        List<NumberDTO> phones = List.of(
                NumberDTO.builder().phoneNumber(123456789).cityCode(32).countryCode(56).build(),
                NumberDTO.builder().phoneNumber(987654321).cityCode(33).countryCode(54).build()
        );
        UserDTO savedUser = assertDoesNotThrow(() -> userService.addUser(correctUser));
        assertNotNull(savedUser);

        assertDoesNotThrow(() -> numberService.addAll(phones, savedUser));
        when(numberService.countByUserId(savedUser.getId())).thenReturn(phones.size());
        int countSavedNmb = numberService.countByUserId(savedUser.getId());
        assertNotEquals(0, countSavedNmb);
        assertEquals(phones.size(), countSavedNmb);
    }

    @Test
    void testInsertCorrectUserWithNameAndWithoutPhoneNumbers(){
        prepareTestForCorrectUser();
        UserDTO saved = assertDoesNotThrow(() -> userService.addUser(correctUser));
        assertNotNull(saved);
        assertNotNull(saved.getCreated());
        assertNotNull(saved.getLastLogin());
        assertNotNull(saved.getIsActive());
        assertEquals(Boolean.TRUE, saved.getIsActive());

        List<NumberDTO> phones = numberService.getByUser(saved.getId());
        assertEquals(0, phones.size());
    }

    @Test
    void whenInsertingUserWithBadMailThenMustThrowException(){
        // Arrange
        UserDTO userDTO = UserDTO.builder()
                .id(UUID.randomUUID().toString())
                .name("First User")
                .email("wrong.mail") // Invalid email
                .password(Base64.getEncoder().encodeToString("2A1bcdefg".getBytes()))
                .build();

        User user = User.builder()
                .id(userDTO.getId())
                .name(userDTO.getName())
                .email(userDTO.getEmail())
                .password(userDTO.getPassword())
                .build();

        String invalidMsg = "Invalid email format";
        // Mock repository.existsByEmail to return false (no existing user)
        when(userRepository.existsByEmail(anyString())).thenReturn(false);

        // Mock mapper.map from UserDTO to User
        when(mapper.map(userDTO, User.class)).thenReturn(user);

        // Mock repository.save to throw ConstraintViolationException
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        Set<ConstraintViolation<?>> violations = new HashSet<>();
        violations.add(violation);
        ConstraintViolationException cve = new ConstraintViolationException(invalidMsg, violations);
        RollbackException rollbackException = new RollbackException("Validation failed", cve);
        when(userRepository.save(user)).thenThrow(new TransactionSystemException("Transaction failed", rollbackException));

        // Act and Assert
        TransactionSystemException exception = assertThrows(TransactionSystemException.class, () -> userService.addUser(userDTO));

        // Verify exception chain
        assertNotNull(exception);
        assertNotNull(exception.getCause());
        assertTrue(exception.getCause() instanceof RollbackException);
        assertNotNull(exception.getCause().getCause());
        assertTrue(exception.getCause().getCause() instanceof ConstraintViolationException);
        assertNotNull(exception.getCause().getCause().getMessage());
        assertThat(exception.getCause().getCause().getMessage(), containsString(invalidMsg));
    }

    @Test
    void whenInsertingExistingUserThenMustThrowException(){
        when(mapper.map(correctUser, User.class)).thenAnswer(i ->
                User.builder()
                    .id(correctUser.getId())
                    .name(correctUser.getName())
                    .email(correctUser.getEmail())
                    .password(correctUser.getPassword())
                    .build()
        );
        when(userService.addUser(correctUser)).thenAnswer(i -> {
            Date today = new Date();
            return UserDTO.builder()
                    .id(correctUser.getId())
                    .name(correctUser.getName())
                    .email(correctUser.getEmail())
                    .password(correctUser.getPassword())
                    .created(today)
                    .lastLogin(today)
                    .isActive(Boolean.TRUE)
                    .build();
        }).thenThrow(UserAlreadyExistsException.class);
        assertDoesNotThrow(() -> userService.addUser(correctUser));
        assertThrows(UserAlreadyExistsException.class, () -> userService.addUser(correctUser));
    }

    @Test
    void whenCheckingIfExistingUserWithSpecifiedMailExistsThenMustReturnTrue(){
        prepareTestForCorrectUser();
        UserDTO saved = assertDoesNotThrow(() -> userService.addUser(correctUser));
        assertNotNull(saved);
        when(userService.existsByEmail(saved.getEmail())).thenReturn(Boolean.TRUE);
        Boolean exists = assertDoesNotThrow(() -> userService.existsByEmail(saved.getEmail()));
        assertTrue(exists);
    }

    @Test
    void whenCheckingIfNonExistingUserWithSpecifiedMailExistsThenMustReturnFalse(){
        Boolean exists = assertDoesNotThrow(() -> userService.existsByEmail(correctUser.getEmail()));
        assertFalse(exists);
    }

    @Test
    void testUpdateLastLogin(){
        prepareTestForCorrectUser();

        UserDTO saved = assertDoesNotThrow(() -> userService.addUser(correctUser));
        assertNotNull(saved);
        Date firstLogin = saved.getLastLogin();
        sleep(1000);
        saved.setLastLogin(new Date());
        when(userService.updateUser(saved)).thenAnswer(i ->
            UserDTO.builder()
                   .id(saved.getId())
                   .name(saved.getName())
                   .email(saved.getEmail())
                   .password(saved.getPassword())
                   .created(saved.getCreated())
                   .lastLogin(new Date())
                   .isActive(saved.getIsActive())
                   .build()
        );
       UserDTO updatedUser = assertDoesNotThrow(() -> userService.updateUser(saved));
       assertNotEquals(firstLogin, updatedUser.getLastLogin());
       assertTrue(firstLogin.before(updatedUser.getLastLogin()));
    }

    @Test
    void whenSearchingExistingUserWithCorrectMailAndPasswordThenMustReturnUser(){
        prepareTestForCorrectUser();
        UserDTO savedUser = assertDoesNotThrow(() -> spy(userService).addUser(correctUser));
        User savedEntity =
            User.builder()
                    .id(savedUser.getId())
                    .name(savedUser.getName())
                    .email(savedUser.getEmail())
                    .password(savedUser.getPassword())
                    .created(savedUser.getCreated())
                    .lastLogin(savedUser.getLastLogin())
                    .isActive(savedUser.getIsActive())
                    .build();
        when(userRepository.findByEmailAndPassword(correctUser.getEmail(), correctUser.getPassword())).thenReturn(savedEntity);

        UserDTO foundUser = assertDoesNotThrow(() ->
            spy(userService).searchUser(correctUser.getEmail(), correctUser.getPassword()));
        assertNotNull(foundUser);
        assertEquals(savedUser.getId(), foundUser.getId());
    }

    @Test
    void whenSearchingNonExistingUserWithCorrectMailAndPasswordThenMustReturnNull(){
        prepareTestForCorrectUser();
        assertDoesNotThrow(() -> userService.addUser(correctUser));
        when(userService.searchUser(eq(correctUser.getEmail()), not(eq(correctUser.getPassword())))).thenReturn(null);
        UserDTO foundUser = assertDoesNotThrow(() -> userService.searchUser(correctUser.getEmail(), "1abcDefg2"));
        assertNull(foundUser);
    }
}
