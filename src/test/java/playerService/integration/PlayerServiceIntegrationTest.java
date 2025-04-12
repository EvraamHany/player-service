package playerService.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import playerService.config.SecurityConfig;
import playerService.dto.LoginRequestDto;
import playerService.dto.PlayerRegistrationDto;
import playerService.dto.SessionResponseDto;
import playerService.dto.TimeLimitDto;
import playerService.exception.TimeLimitExceededException;
import playerService.model.Player;
import playerService.repository.PlayerRepository;
import playerService.repository.SessionRepository;
import playerService.service.PlayerService;
import playerService.service.SessionService;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@Import(SecurityConfig.class)
public class PlayerServiceIntegrationTest {

    @Autowired
    private PlayerService playerService;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private SessionRepository sessionRepository;


    @BeforeEach
    void setUp() {
        sessionRepository.deleteAll();
        playerRepository.deleteAll();

    }

    @Test
    void fullPlayerLifecycle() {
        PlayerRegistrationDto registrationDto = new PlayerRegistrationDto(
                "integration@test.com",
                "securepass",
                "Integration",
                "Test",
                LocalDate.of(1995, 5, 15),
                "456 Integration St, Test City"
        );

        Player registeredPlayer = playerService.registerPlayer(registrationDto);
        assertNotNull(registeredPlayer);
        assertEquals("integration@test.com", registeredPlayer.getEmail());
        assertTrue(registeredPlayer.isActive());

        LoginRequestDto loginRequest = new LoginRequestDto("integration@test.com", "securepass");
        SessionResponseDto sessionResponse = sessionService.login(loginRequest);
        assertNotNull(sessionResponse);
        assertNotNull(sessionResponse.getSessionId());
        assertEquals("integration@test.com", sessionResponse.getPlayerEmail());

        String sessionId = sessionResponse.getSessionId();

        TimeLimitDto timeLimitDto = new TimeLimitDto(registeredPlayer.getId(), 60);
        Player playerWithLimit = playerService.setTimeLimit(timeLimitDto);
        assertEquals(60, playerWithLimit.getDailyTimeLimit());

        sessionService.logout(sessionId);

        SessionResponseDto newSessionResponse = sessionService.login(loginRequest);
        assertNotNull(newSessionResponse);

        sessionService.logout(newSessionResponse.getSessionId());
    }

    @Test
    void timeLimitEnforcement() {
        PlayerRegistrationDto registrationDto = new PlayerRegistrationDto(
                "timelimit@test.com",
                "limitpass",
                "Time",
                "Limit",
                LocalDate.of(1990, 10, 20),
                "789 Limit St, Test City"
        );

        Player registeredPlayer = playerService.registerPlayer(registrationDto);

        TimeLimitDto timeLimitDto = new TimeLimitDto(registeredPlayer.getId(), 1);
        playerService.setTimeLimit(timeLimitDto);

        LoginRequestDto loginRequest = new LoginRequestDto("timelimit@test.com", "limitpass");
        SessionResponseDto sessionResponse = sessionService.login(loginRequest);
        String sessionId = sessionResponse.getSessionId();

        Player player = playerService.getPlayerById(registeredPlayer.getId());
        playerService.updatePlayerSessionTime(player, 70);

        sessionService.logout(sessionId);

        assertThrows(TimeLimitExceededException.class, () -> {
            sessionService.login(loginRequest);
        });
    }
}
