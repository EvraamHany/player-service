package playerService.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import playerService.dto.LoginRequestDto;
import playerService.dto.PlayerRegistrationDto;
import playerService.dto.SessionResponseDto;
import playerService.dto.TimeLimitDto;
import playerService.exception.TimeLimitExceededException;
import playerService.model.Player;
import playerService.service.PlayerService;
import playerService.service.SessionService;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class PlayerServiceIntegrationTest {

    @Autowired
    private PlayerService playerService;

    @Autowired
    private SessionService sessionService;

    @Test
    void fullPlayerLifecycle() {
        // 1. Register a new player
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

        // 2. Login the player
        LoginRequestDto loginRequest = new LoginRequestDto("integration@test.com", "securepass");
        SessionResponseDto sessionResponse = sessionService.login(loginRequest);
        assertNotNull(sessionResponse);
        assertNotNull(sessionResponse.getSessionId());
        assertEquals("integration@test.com", sessionResponse.getPlayerEmail());

        String sessionId = sessionResponse.getSessionId();

        // 3. Set time limit for the player
        TimeLimitDto timeLimitDto = new TimeLimitDto(registeredPlayer.getId(), 60); // 60 minutes
        Player playerWithLimit = playerService.setTimeLimit(timeLimitDto);
        assertEquals(60, playerWithLimit.getDailyTimeLimit());

        // 4. Logout the player
        sessionService.logout(sessionId);

        // 5. Login again
        SessionResponseDto newSessionResponse = sessionService.login(loginRequest);
        assertNotNull(newSessionResponse);

        // Cleanup
        sessionService.logout(newSessionResponse.getSessionId());
    }

    @Test
    void timeLimitEnforcement() {
        // 1. Register a player
        PlayerRegistrationDto registrationDto = new PlayerRegistrationDto(
                "timelimit@test.com",
                "limitpass",
                "Time",
                "Limit",
                LocalDate.of(1990, 10, 20),
                "789 Limit St, Test City"
        );

        Player registeredPlayer = playerService.registerPlayer(registrationDto);

        // 2. Set a very small time limit (1 minute)
        TimeLimitDto timeLimitDto = new TimeLimitDto(registeredPlayer.getId(), 1); // 1 minute
        playerService.setTimeLimit(timeLimitDto);

        // 3. Login the player
        LoginRequestDto loginRequest = new LoginRequestDto("timelimit@test.com", "limitpass");
        SessionResponseDto sessionResponse = sessionService.login(loginRequest);
        String sessionId = sessionResponse.getSessionId();

        // 4. Simulate time passing - manually update the session time to exceed the limit
        Player player = playerService.getPlayerById(registeredPlayer.getId());
        playerService.updatePlayerSessionTime(player, 70); // 70 seconds (> 1 minute limit)

        // 5. Logout
        sessionService.logout(sessionId);

        // 6. Try to login again - should be prevented due to time limit
        assertThrows(TimeLimitExceededException.class, () -> {
            sessionService.login(loginRequest);
        });
    }
}
