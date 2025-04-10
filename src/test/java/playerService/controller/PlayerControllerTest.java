package playerService.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import playerService.dto.PlayerRegistrationDto;
import playerService.dto.TimeLimitDto;
import playerService.exception.PlayerAlreadyExistsException;
import playerService.exception.PlayerInactiveException;
import playerService.model.Player;
import playerService.service.PlayerService;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PlayerController.class)
public class PlayerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PlayerService playerService;

    @Autowired
    private ObjectMapper objectMapper;

    private PlayerRegistrationDto validRegistration;
    private Player validPlayer;
    private TimeLimitDto validTimeLimit;

    @BeforeEach
    void setUp() {
        validRegistration = new PlayerRegistrationDto(
                "test@example.com",
                "password123",
                "John",
                "Doe",
                LocalDate.of(1990, 1, 1),
                "123 Test St, Test City"
        );

        validPlayer = new Player();
        validPlayer.setId(1L);
        validPlayer.setEmail("test@example.com");
        validPlayer.setName("John");
        validPlayer.setSurname("Doe");
        validPlayer.setDateOfBirth(LocalDate.of(1990, 1, 1));
        validPlayer.setAddress("123 Test St, Test City");
        validPlayer.setActive(true);

        validTimeLimit = new TimeLimitDto(1L, 120); // 120 minutes daily limit
    }

    @Test
    void registerPlayer_Success() throws Exception {
        when(playerService.registerPlayer(any(PlayerRegistrationDto.class))).thenReturn(validPlayer);

        mockMvc.perform(post("/api/players/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRegistration)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(validPlayer.getEmail()))
                .andExpect(jsonPath("$.name").value(validPlayer.getName()))
                .andExpect(jsonPath("$.surname").value(validPlayer.getSurname()))
                .andExpect(jsonPath("$.active").value(validPlayer.isActive()));
    }

    @Test
    void registerPlayer_AlreadyExists() throws Exception {
        when(playerService.registerPlayer(any(PlayerRegistrationDto.class)))
                .thenThrow(new PlayerAlreadyExistsException("Player already exists"));

        mockMvc.perform(post("/api/players/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRegistration)))
                .andExpect(status().isConflict());
    }

    @Test
    void registerPlayer_InvalidInput() throws Exception {
        PlayerRegistrationDto invalidRegistration = new PlayerRegistrationDto(
                "", // Empty email
                "password123",
                "John",
                "Doe",
                LocalDate.of(1990, 1, 1),
                "123 Test St, Test City"
        );

        mockMvc.perform(post("/api/players/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRegistration)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void setTimeLimit_Success() throws Exception {
        Player playerWithLimit = validPlayer;
        playerWithLimit.setDailyTimeLimit(120);

        when(playerService.setTimeLimit(any(TimeLimitDto.class))).thenReturn(playerWithLimit);

        mockMvc.perform(post("/api/players/time-limit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validTimeLimit)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dailyTimeLimit").value(120));
    }

    @Test
    void setTimeLimit_PlayerInactive() throws Exception {
        when(playerService.setTimeLimit(any(TimeLimitDto.class)))
                .thenThrow(new PlayerInactiveException("Cannot set time limit for inactive player"));

        mockMvc.perform(post("/api/players/time-limit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validTimeLimit)))
                .andExpect(status().isBadRequest());
    }
}
