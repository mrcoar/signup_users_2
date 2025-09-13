package cl.maraneda.signup.controller;

import cl.maraneda.signup.ApplicationConfig;
import cl.maraneda.signup.dto.CreatedUserDTO;
import cl.maraneda.signup.dto.InputUserDTO;
import cl.maraneda.signup.dto.NumberDTO;
import cl.maraneda.signup.dto.UserDTO;
import cl.maraneda.signup.repository.NumberRepository;
import cl.maraneda.signup.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.core.Is;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

import static cl.maraneda.signup.Util.JWT_PATTERN;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(ApplicationConfig.class)
public class UserControllerTest {
    private static final String USER_PATH = "/user";
    private static final String SIGNUP_PATH = USER_PATH + "/sign-up";
    private static final String CORRECT_PASSWORD = "1A2bcdefg";
    private static final String ENCODED_CPASSWORD = Base64.getEncoder().encodeToString(CORRECT_PASSWORD.getBytes());
    private static final String ID_JSON_KEY = "$.id";
    private static final String NAME_JSON_KEY = "$.name";
    private static final String EMAIL_JSON_KEY = "$.email";
    private static final String PWD_JSON_KEY = "$.password";
    private static final String CREAT_JSON_KEY = "$.created";
    private static final String LLOG_JSON_KEY = "$.lastLogin";
    private static final String TOKEN_JSON_KEY = "$.token";
    private static final String DET_JSON_KEY = "$.detail";
    private static final String ACTIVE_JSON_KEY = "$.isActive";

    @Autowired
    private transient MockMvc mockMvc;

    @Autowired
    protected transient ObjectMapper objectMapper;

    @Autowired
    transient ModelMapper modelMapper;

    private transient final UserDTO correctUser =
            UserDTO.builder()
                    .id(UUID.randomUUID().toString())
                    .name("First User")
                    .email(String.format("email.%s@domain.ext", UUID.randomUUID().toString().replace("-", "")))
                    .password(ENCODED_CPASSWORD)
                    .build();
    private transient final InputUserDTO correctInput =
            InputUserDTO.builder()
                    .id(correctUser.getId())
                    .name(correctUser.getName())
                    .email(correctUser.getEmail())
                    .password(CORRECT_PASSWORD)
                    .build();
    private transient final Pattern uuidPattern =
        Pattern.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$");

    private transient final List<NumberDTO> phones = List.of(
            NumberDTO.builder().phoneNumber(123456789).cityCode(32).countryCode(56).build(),
            NumberDTO.builder().phoneNumber(987654321).cityCode(33).countryCode(55).build()
    );

    @Test
    void whenAddingUserWithNameAndCorrectMailAndCorrectPasswordThenMustReturn201StatusCode() throws Exception {

        InputUserDTO input = modelMapper.map(correctInput, InputUserDTO.class);
        input.setPhones(phones);

        String inputContent = objectMapper.writeValueAsString(correctInput);
        MvcResult resp =
            mockMvc.perform(put(SIGNUP_PATH).contentType(MediaType.APPLICATION_JSON).content(inputContent))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath(ID_JSON_KEY, allOf(notNullValue(), matchesPattern(uuidPattern))))
                .andExpect(jsonPath(NAME_JSON_KEY, Is.is(correctUser.getName())))
                .andExpect(jsonPath(EMAIL_JSON_KEY, Is.is(correctUser.getEmail())))
                .andExpect(jsonPath(PWD_JSON_KEY, Is.is(CORRECT_PASSWORD)))
                .andExpect(jsonPath(CREAT_JSON_KEY, notNullValue()))
                .andExpect(jsonPath(LLOG_JSON_KEY, notNullValue()))
                .andExpect(jsonPath(ACTIVE_JSON_KEY, Is.is(Boolean.TRUE)))
                .andExpect(jsonPath(TOKEN_JSON_KEY, allOf(notNullValue(), matchesPattern(JWT_PATTERN))))
                .andExpect(jsonPath("$.phones", allOf(notNullValue(), hasSize(phones.size()))))
                .andReturn();

        String content = resp.getResponse().getContentAsString();
        CreatedUserDTO outputUser = objectMapper.readValue(content, CreatedUserDTO.class);

        assertTrue(phones.containsAll(outputUser.getPhones()));
    }

    @Test
    void whenAddingUserWithoutNameAndCorrectMailAndCorrectPasswordThenMustReturn201StatusCode() throws Exception {
        InputUserDTO input = modelMapper.map(correctInput, InputUserDTO.class);
        input.setName(null);
        String inputContent = objectMapper.writeValueAsString(correctInput);
        mockMvc.perform(put(SIGNUP_PATH).contentType(MediaType.APPLICATION_JSON).content(inputContent))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath(DET_JSON_KEY, Is.is("Name is required to sign up the user")));
    }

    @Test
    void whenAddingUserWithNameAndWithoutMailAndWithCorrectPasswordThenMustReturn400StatusCode() throws Exception {
        InputUserDTO input =
            InputUserDTO.builder()
                    .id(correctUser.getId())
                    .name(correctUser.getName())
                    .password(CORRECT_PASSWORD)
                    .build();
        String inputContent = objectMapper.writeValueAsString(input);
        mockMvc.perform(put(SIGNUP_PATH).contentType(MediaType.APPLICATION_JSON).content(inputContent))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath(DET_JSON_KEY, Is.is("Email is required to sign up the user")));
    }

    @Test
    void whenAddingUserWithNameAndWithMailAndWithoutPasswordThenMustReturn400StatusCode() throws Exception {
        InputUserDTO input =
            InputUserDTO.builder()
                    .id(correctUser.getId())
                    .name(correctUser.getName())
                    .email(correctInput.getEmail())
                    .build();
        String inputContent = objectMapper.writeValueAsString(input);
        mockMvc.perform(put(SIGNUP_PATH).contentType(MediaType.APPLICATION_JSON).content(inputContent))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath(DET_JSON_KEY, Is.is("Password is required to sign up the user")));
    }

    @Test
    void whenAddingUserWithNameAndWithWrongMailAndWithCorrectPasswordThenMustReturn400StatusCode() throws Exception {
        InputUserDTO input =
                InputUserDTO.builder()
                        .id(correctUser.getId())
                        .name(correctUser.getName())
                        .email("wrong email")
                        .password(CORRECT_PASSWORD)
                        .build();
        String inputContent = objectMapper.writeValueAsString(input);
        mockMvc.perform(put(SIGNUP_PATH).contentType(MediaType.APPLICATION_JSON).content(inputContent))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath(DET_JSON_KEY, startsWith("The user must have at least one phone number")));
    }

    @Test
    void whenAddingUserWithNameAndWithCorrectMailAndWithWrongPasswordThenMustReturn400StatusCode() throws Exception {
        InputUserDTO input =
                InputUserDTO.builder()
                        .id(correctUser.getId())
                        .name(correctUser.getName())
                        .email(correctUser.getEmail())
                        .password("wrong password")
                        .build();
        String inputContent = objectMapper.writeValueAsString(input);
        mockMvc.perform(put(SIGNUP_PATH).contentType(MediaType.APPLICATION_JSON).content(inputContent))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath(DET_JSON_KEY, startsWith("The password must contain between 8 and 12 characters.")));
    }

    @Test
    void whenAddingAlreadyExistingUserThenMustReturn403StatusCode() throws Exception {
        InputUserDTO input = modelMapper.map(correctInput, InputUserDTO.class);
        input.setPhones(phones);

        String inputContent = objectMapper.writeValueAsString(correctInput);
        mockMvc.perform(put(SIGNUP_PATH).contentType(MediaType.APPLICATION_JSON).content(inputContent))
                .andExpect(status().isCreated());
        mockMvc.perform(put(SIGNUP_PATH).contentType(MediaType.APPLICATION_JSON).content(inputContent))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath(DET_JSON_KEY, Is.is("The user already exists")));
    }

    @AfterAll
    static void cleanDatabase(@Autowired NumberRepository numberRepository, @Autowired UserRepository userRepository){
        numberRepository.deleteByUserEmailPrefix("email.");
        userRepository.deleteByUserEmailPrefix("email.");
    }
}