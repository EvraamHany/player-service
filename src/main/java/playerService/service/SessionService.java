package playerService.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import playerService.dto.LoginRequestDto;
import playerService.dto.SessionResponseDto;
import playerService.exception.InvalidCredentialsException;
import playerService.exception.SessionNotFoundException;
import playerService.exception.TimeLimitExceededException;
import playerService.model.Player;
import playerService.model.Session;
import playerService.repository.SessionRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class SessionService {

    private final SessionRepository sessionRepository;
    private final PlayerService playerService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public SessionService(SessionRepository sessionRepository, PlayerService playerService, PasswordEncoder passwordEncoder) {
        this.sessionRepository = sessionRepository;
        this.playerService = playerService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public SessionResponseDto login(LoginRequestDto loginRequest) {
        Player player = playerService.getPlayerByEmail(loginRequest.getEmail());

        if (!passwordEncoder.matches(loginRequest.getPassword(), player.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        if (playerService.hasExceededTimeLimit(player)) {
            throw new TimeLimitExceededException("Daily time limit exceeded");
        }

        List<Session> activeSessions = sessionRepository.findByPlayerAndLoggedOutAtIsNull(player);
        for (Session activeSession : activeSessions) {
            logout(activeSession.getId());
        }

        Session session = new Session();
        session.setPlayer(player);
        session = sessionRepository.save(session);

        playerService.updateSessionStartTime(player);

        return convertToDto(session);
    }

    @Transactional
    public void logout(String sessionId) {
        Session session = sessionRepository.findByIdAndLoggedOutAtIsNull(sessionId)
                .orElseThrow(() -> new SessionNotFoundException("Active session not found with id: " + sessionId));

        LocalDateTime now = LocalDateTime.now();
        session.setLoggedOutAt(now);
        sessionRepository.save(session);

        Player player = session.getPlayer();
        LocalDateTime sessionStart = player.getLastSessionStart() != null ?
                player.getLastSessionStart() : session.getCreatedAt();
        long sessionDurationSeconds = Duration.between(sessionStart, now).getSeconds();
        playerService.updatePlayerSessionTime(player, sessionDurationSeconds);
    }

    @Transactional
    public void checkAndLogoutTimeLimitExceededPlayers() {
        List<Session> activeSessions = sessionRepository.findAll().stream()
                .filter(s -> s.getLoggedOutAt() == null)
                .toList();

        for (Session session : activeSessions) {
            Player player = session.getPlayer();

            if (player.getDailyTimeLimit() == null) {
                continue;
            }

            LocalDateTime sessionStart = player.getLastSessionStart() != null ?
                    player.getLastSessionStart() : session.getCreatedAt();
            long currentSessionSeconds = Duration.between(sessionStart, LocalDateTime.now()).getSeconds();

            long totalSessionTime = player.getTodaySessionTime() + currentSessionSeconds;
            if (totalSessionTime >= player.getDailyTimeLimit() * 60) {
                logout(session.getId());
            }
        }
    }

    private SessionResponseDto convertToDto(Session session) {
        return new SessionResponseDto(
                session.getId(),
                session.getPlayer().getEmail(),
                session.getCreatedAt(),
                session.getExpiresAt()
        );
    }
}
