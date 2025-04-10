package playerService.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import playerService.dto.LoginRequestDto;
import playerService.dto.SessionResponseDto;
import playerService.exception.InvalidCredentialsException;
import playerService.exception.SessionNotFoundException;
import playerService.exception.TimeLimitExceededException;
import playerService.model.Player;
import playerService.model.Session;
import playerService.repository.SessionRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SessionServiceTest {

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private PlayerService playerService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private SessionService sessionService;

    private LoginRequestDto validLoginRequest;
    private Player validPlayer;
    private Session validSession;

    @BeforeEach
    void setUp() {
        validLoginRequest = new LoginRequestDto("test@example.com", "password123");

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

        LocalDateTime now = LocalDateTime.now();
        validSession = new Session();
        validSession.setId("session-123");
        validSession.setPlayer(validPlayer);
        validSession.setCreatedAt(now);
        validSession.setExpiresAt(now.plusHours(24));
    }

    @Test
    void login_Success() {
        when(playerService.getPlayerByEmail(anyString())).thenReturn(validPlayer);
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(playerService.hasExceededTimeLimit(any(Player.class))).thenReturn(false);
        when(sessionRepository.findByPlayerAndLoggedOutAtIsNull(any(Player.class))).thenReturn(new ArrayList<>());
        when(sessionRepository.save(any(Session.class))).thenReturn(validSession);
        doNothing().when(playerService).updateSessionStartTime(any(Player.class));

        SessionResponseDto result = sessionService.login(validLoginRequest);

        assertNotNull(result);
        assertEquals(validSession.getId(), result.getSessionId());
        assertEquals(validPlayer.getEmail(), result.getPlayerEmail());
    }

    @Test
    void login_InvalidCredentials() {
        when(playerService.getPlayerByEmail(anyString())).thenReturn(validPlayer);
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> {
            sessionService.login(validLoginRequest);
        });
    }

    @Test
    void login_TimeLimitExceeded() {
        when(playerService.getPlayerByEmail(anyString())).thenReturn(validPlayer);
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(playerService.hasExceededTimeLimit(any(Player.class))).thenReturn(true);

        assertThrows(TimeLimitExceededException.class, () -> {
            sessionService.login(validLoginRequest);
        });
    }

    @Test
    void login_LogoutExistingSessions() {
        Session existingSession = new Session();
        existingSession.setId("existing-session");
        existingSession.setPlayer(validPlayer);
        List<Session> existingSessions = List.of(existingSession);

        when(playerService.getPlayerByEmail(anyString())).thenReturn(validPlayer);
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(playerService.hasExceededTimeLimit(any(Player.class))).thenReturn(false);
        when(sessionRepository.findByPlayerAndLoggedOutAtIsNull(any(Player.class))).thenReturn(existingSessions);
        when(sessionRepository.findByIdAndLoggedOutAtIsNull(eq("existing-session"))).thenReturn(Optional.of(existingSession));
        when(sessionRepository.save(any(Session.class))).thenReturn(validSession).thenReturn(existingSession);
        doNothing().when(playerService).updateSessionStartTime(any(Player.class));
        doNothing().when(playerService).updatePlayerSessionTime(any(Player.class), anyLong());

        SessionResponseDto result = sessionService.login(validLoginRequest);

        assertNotNull(result);
        verify(sessionRepository, times(2)).save(any(Session.class)); // Once for logout, once for new session
    }

    @Test
    void logout_Success() {
        when(sessionRepository.findByIdAndLoggedOutAtIsNull(anyString())).thenReturn(Optional.of(validSession));
        when(sessionRepository.save(any(Session.class))).thenReturn(validSession);
        doNothing().when(playerService).updatePlayerSessionTime(any(Player.class), anyLong());

        assertDoesNotThrow(() -> {
            sessionService.logout("session-123");
        });

        verify(sessionRepository).save(any(Session.class));
        verify(playerService).updatePlayerSessionTime(eq(validPlayer), anyLong());
    }

    @Test
    void logout_SessionNotFound() {
        when(sessionRepository.findByIdAndLoggedOutAtIsNull(anyString())).thenReturn(Optional.empty());

        assertThrows(SessionNotFoundException.class, () -> {
            sessionService.logout("invalid-session");
        });
    }

    @Test
    void checkAndLogoutTimeLimitExceededPlayers() {
        // Setup a player with time limit
        Player playerWithTimeLimit = validPlayer;
        playerWithTimeLimit.setDailyTimeLimit(60); // 60 minutes
        playerWithTimeLimit.setTodaySessionTime(3500L); // 58 minutes and 20 seconds
        playerWithTimeLimit.setLastSessionStart(LocalDateTime.now().minusMinutes(5)); // Session started 5 minutes ago

        Session session = validSession;
        session.setPlayer(playerWithTimeLimit);

        when(sessionRepository.findAll()).thenReturn(List.of(session));
        when(sessionRepository.findByIdAndLoggedOutAtIsNull(anyString())).thenReturn(Optional.of(session));
        when(sessionRepository.save(any(Session.class))).thenReturn(session);
        doNothing().when(playerService).updatePlayerSessionTime(any(Player.class), anyLong());

        // Execute
        sessionService.checkAndLogoutTimeLimitExceededPlayers();

        // Verify that the session was logged out
        verify(sessionRepository).save(any(Session.class));
    }
}
