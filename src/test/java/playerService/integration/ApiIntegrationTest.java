package playerService.integration;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
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
public class ApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void fullApiWorkflow() throws Exception {
        // 1. Register a new player
        PlayerRegistrationDto registrationDto = new PlayerRegistrationDto(
                "api@test.com",
                "apipass",
                "API",
                "Test",
                LocalDate.of(1992, 8, 15),
                "123 API St, Test City"
        );

// Register player
        MvcResult registerResult = mockMvc.perform(post("/api/players/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("api@test.com"))
                .andExpect(jsonPath("$.name").value("API"))
                .andExpect(jsonPath("$.surname").value("Test"))
                .andExpect(jsonPath("$.active").value(true))
                .andReturn();

// Extract player ID from response
        String registerResponse = registerResult.getResponse().getContentAsString();
        Long playerId = objectMapper.readTree(registerResponse).get("id").asLong();

// 2. Login the player
        LoginRequestDto loginRequest = new LoginRequestDto("api@test.com", "apipass");

        MvcResult loginResult = mockMvc.perform(post("/api/sessions/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.playerEmail").value("api@test.com"))
                .andExpect(jsonPath("$.sessionId").isNotEmpty())
                .andReturn();

// Extract session ID
        String loginResponse = loginResult.getResponse().getContentAsString();
        String sessionId = objectMapper.readTree(loginResponse).get("sessionId").asText();

// 3. Set time limit
        TimeLimitDto timeLimitDto = new TimeLimitDto(playerId, 120); // 2 hours daily limit

        mockMvc.perform(post("/api/players/time-limit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(timeLimitDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dailyTimeLimit").value(120));

// 4. Logout
        mockMvc.perform(post("/api/sessions/logout/{sessionId}", sessionId))
                .andExpect(status().isOk());

// 5. Login again after logout
        mockMvc.perform(post("/api/sessions/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.playerEmail").value("api@test.com"));
    }

    @Test
    void invalidLogin() throws Exception {
        // First register a player
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

        // Try to login with wrong password
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
        // Register first player
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

        // Try to register second player with same email
        PlayerRegistrationDto duplicateRegistration = new PlayerRegistrationDto(
                "duplicate@test.com", // Same email
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

