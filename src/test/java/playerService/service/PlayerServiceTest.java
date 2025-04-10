package playerService.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import playerService.dto.PlayerRegistrationDto;
import playerService.dto.TimeLimitDto;
import playerService.exception.PlayerAlreadyExistsException;
import playerService.exception.PlayerInactiveException;
import playerService.exception.PlayerNotFoundException;
import playerService.model.Player;
import playerService.repository.PlayerRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PlayerServiceTest {

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private PlayerService playerService;

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
        validPlayer.setPassword("encoded_password");
        validPlayer.setName("John");
        validPlayer.setSurname("Doe");
        validPlayer.setDateOfBirth(LocalDate.of(1990, 1, 1));
        validPlayer.setAddress("123 Test St, Test City");
        validPlayer.setActive(true);
        validPlayer.setLastDailyReset(LocalDateTime.now());
        validPlayer.setTodaySessionTime(0L);

        validTimeLimit = new TimeLimitDto(1L, 120); // 120 minutes daily limit
    }

    @Test
    void registerPlayer_Success() {
        when(playerRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded_password");
        when(playerRepository.save(any(Player.class))).thenReturn(validPlayer);

        Player result = playerService.registerPlayer(validRegistration);

        assertNotNull(result);
        assertEquals(validPlayer.getEmail(), result.getEmail());
        assertEquals(validPlayer.getName(), result.getName());
        assertTrue(result.isActive());
    }

    @Test
    void registerPlayer_AlreadyExists() {
        when(playerRepository.existsByEmail(anyString())).thenReturn(true);

        assertThrows(PlayerAlreadyExistsException.class, () -> {
            playerService.registerPlayer(validRegistration);
        });
    }

    @Test
    void getPlayerById_Success() {
        when(playerRepository.findById(anyLong())).thenReturn(Optional.of(validPlayer));

        Player result = playerService.getPlayerById(1L);

        assertNotNull(result);
        assertEquals(validPlayer.getId(), result.getId());
    }

    @Test
    void getPlayerById_NotFound() {
        when(playerRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(PlayerNotFoundException.class, () -> {
            playerService.getPlayerById(1L);
        });
    }

    @Test
    void getPlayerByEmail_Success() {
        when(playerRepository.findByEmail(anyString())).thenReturn(Optional.of(validPlayer));

        Player result = playerService.getPlayerByEmail("test@example.com");

        assertNotNull(result);
        assertEquals(validPlayer.getEmail(), result.getEmail());
    }

    @Test
    void getPlayerByEmail_NotFound() {
        when(playerRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThrows(PlayerNotFoundException.class, () -> {
            playerService.getPlayerByEmail("test@example.com");
        });
    }

    @Test
    void setTimeLimit_Success() {
        when(playerRepository.findById(anyLong())).thenReturn(Optional.of(validPlayer));
        when(playerRepository.save(any(Player.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Player result = playerService.setTimeLimit(validTimeLimit);

        assertNotNull(result);
        assertEquals(validTimeLimit.getDailyLimitMinutes(), result.getDailyTimeLimit());
    }

    @Test
    void setTimeLimit_PlayerInactive() {
        Player inactivePlayer = validPlayer;
        inactivePlayer.setActive(false);

        when(playerRepository.findById(anyLong())).thenReturn(Optional.of(inactivePlayer));

        assertThrows(PlayerInactiveException.class, () -> {
            playerService.setTimeLimit(validTimeLimit);
        });
    }

    @Test
    void hasExceededTimeLimit_True() {
        Player playerWithTimeLimit = validPlayer;
        playerWithTimeLimit.setDailyTimeLimit(60); // 60 minutes limit
        playerWithTimeLimit.setTodaySessionTime(3601L); // 60 minutes + 1 second

        boolean result = playerService.hasExceededTimeLimit(playerWithTimeLimit);

        assertTrue(result);
    }

    @Test
    void hasExceededTimeLimit_False() {
        Player playerWithTimeLimit = validPlayer;
        playerWithTimeLimit.setDailyTimeLimit(60); // 60 minutes limit
        playerWithTimeLimit.setTodaySessionTime(3500L); // 58 minutes + 20 seconds

        boolean result = playerService.hasExceededTimeLimit(playerWithTimeLimit);

        assertFalse(result);
    }

    @Test
    void hasExceededTimeLimit_NoLimit() {
        Player playerWithoutTimeLimit = validPlayer;
        playerWithoutTimeLimit.setDailyTimeLimit(null);

        boolean result = playerService.hasExceededTimeLimit(playerWithoutTimeLimit);

        assertFalse(result);
    }

    @Test
    void updatePlayerSessionTime_Success() {
        when(playerRepository.save(any(Player.class))).thenAnswer(invocation -> invocation.getArgument(0));

        playerService.updatePlayerSessionTime(validPlayer, 300); // Add 5 minutes (300 seconds)

        assertEquals(300L, validPlayer.getTodaySessionTime());
    }

    @Test
    void updatePlayerSessionTime_NewDay() {
        Player playerLastPlayedYesterday = validPlayer;
        playerLastPlayedYesterday.setLastDailyReset(LocalDateTime.now().minusDays(1));
        playerLastPlayedYesterday.setTodaySessionTime(1800L); // 30 minutes from yesterday

        when(playerRepository.save(any(Player.class))).thenAnswer(invocation -> invocation.getArgument(0));

        playerService.updatePlayerSessionTime(playerLastPlayedYesterday, 300); // Add 5 minutes (300 seconds)

        assertEquals(300L, playerLastPlayedYesterday.getTodaySessionTime()); // Should reset to 0 + 300
        assertEquals(LocalDate.now(), playerLastPlayedYesterday.getLastDailyReset().toLocalDate());
    }
}
