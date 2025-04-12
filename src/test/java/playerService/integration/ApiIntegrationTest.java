package playerService.integration;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import playerService.config.SecurityConfig;
import playerService.dto.LoginRequestDto;
import playerService.dto.PlayerRegistrationDto;
import playerService.dto.TimeLimitDto;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@Import(SecurityConfig.class)
public class ApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void fullApiWorkflow() throws Exception {
        PlayerRegistrationDto registrationDto = new PlayerRegistrationDto(
                "api@test.com",
                "apipass",
                "API",
                "Test",
                LocalDate.of(1992, 8, 15),
                "123 API St, Test City"
        );

        MvcResult registerResult = mockMvc.perform(post("/api/players/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("api@test.com"))
                .andExpect(jsonPath("$.name").value("API"))
                .andExpect(jsonPath("$.surname").value("Test"))
                .andExpect(jsonPath("$.active").value(true))
                .andReturn();

        String registerResponse = registerResult.getResponse().getContentAsString();
        Long playerId = objectMapper.readTree(registerResponse).get("id").asLong();

        LoginRequestDto loginRequest = new LoginRequestDto("api@test.com", "apipass");

        MvcResult loginResult = mockMvc.perform(post("/api/sessions/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.playerEmail").value("api@test.com"))
                .andExpect(jsonPath("$.sessionId").isNotEmpty())
                .andReturn();


        String loginResponse = loginResult.getResponse().getContentAsString();
        String sessionId = objectMapper.readTree(loginResponse).get("sessionId").asText();


        TimeLimitDto timeLimitDto = new TimeLimitDto(playerId, 120);

        mockMvc.perform(post("/api/players/time-limit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(timeLimitDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dailyTimeLimit").value(120));

        mockMvc.perform(post("/api/sessions/logout/{sessionId}", sessionId))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/sessions/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.playerEmail").value("api@test.com"));
    }

    @Test
    void invalidLogin() throws Exception {
        PlayerRegistrationDto registrationDto = new PlayerRegistrationDto(
                "badlogin@test.com",
                "correctpass",
                "Bad",
                "Login",
                LocalDate.of(1995, 5, 15),
                "123 Login St, Test City"
        );

        mockMvc.perform(post("/api/players/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationDto)))
                .andExpect(status().isCreated());

        LoginRequestDto wrongLoginRequest = new LoginRequestDto("badlogin@test.com", "wrongpass");

        mockMvc.perform(post("/api/sessions/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wrongLoginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void logoutInvalidSession() throws Exception {
        mockMvc.perform(post("/api/sessions/logout/{sessionId}", "non-existent-session"))
                .andExpect(status().isNotFound());
    }

    @Test
    void registerDuplicateEmail() throws Exception {
        PlayerRegistrationDto firstRegistration = new PlayerRegistrationDto(
                "duplicate@test.com",
                "firstpass",
                "First",
                "User",
                LocalDate.of(1990, 1, 1),
                "123 First St, Test City"
        );

        mockMvc.perform(post("/api/players/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(firstRegistration)))
                .andExpect(status().isCreated());

        PlayerRegistrationDto duplicateRegistration = new PlayerRegistrationDto(
                "duplicate@test.com",
                "secondpass",
                "Second",
                "User",
                LocalDate.of(1995, 5, 5),
                "456 Second St, Test City"
        );

        mockMvc.perform(post("/api/players/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateRegistration)))
                .andExpect(status().isConflict());
    }
}

