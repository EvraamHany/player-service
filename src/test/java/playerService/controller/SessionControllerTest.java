package playerService.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import playerService.config.SecurityConfig;
import playerService.dto.LoginRequestDto;
import playerService.dto.SessionResponseDto;
import playerService.exception.InvalidCredentialsException;
import playerService.exception.SessionNotFoundException;
import playerService.exception.TimeLimitExceededException;
import playerService.service.SessionService;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SessionController.class)
@Import(SecurityConfig.class)
public class SessionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SessionService sessionService;

    @Autowired
    private ObjectMapper objectMapper;

    private LoginRequestDto validLoginRequest;
    private SessionResponseDto validSessionResponse;

    @BeforeEach
    void setUp() {
        validLoginRequest = new LoginRequestDto("test@example.com", "password123");

        LocalDateTime now = LocalDateTime.now();
        validSessionResponse = new SessionResponseDto(
                "session-123",
                "test@example.com",
                now,
                now.plusHours(24)
        );
    }

    @Test
    void login_Success() throws Exception {
        when(sessionService.login(any(LoginRequestDto.class))).thenReturn(validSessionResponse);

        mockMvc.perform(post("/api/sessions/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId").value(validSessionResponse.getSessionId()))
                .andExpect(jsonPath("$.playerEmail").value(validSessionResponse.getPlayerEmail()));
    }

    @Test
    void login_InvalidCredentials() throws Exception {
        when(sessionService.login(any(LoginRequestDto.class)))
                .thenThrow(new InvalidCredentialsException("Invalid email or password"));

        mockMvc.perform(post("/api/sessions/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_TimeLimitExceeded() throws Exception {
        when(sessionService.login(any(LoginRequestDto.class)))
                .thenThrow(new TimeLimitExceededException("Daily time limit exceeded"));

        mockMvc.perform(post("/api/sessions/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    void logout_Success() throws Exception {
        mockMvc.perform(post("/api/sessions/logout/{sessionId}", "session-123"))
                .andExpect(status().isOk());
    }

    @Test
    void logout_SessionNotFound() throws Exception {
        doThrow(new SessionNotFoundException("Session not found"))
                .when(sessionService).logout(anyString());

        mockMvc.perform(post("/api/sessions/logout/{sessionId}", "invalid-session"))
                .andExpect(status().isNotFound());
    }
}

