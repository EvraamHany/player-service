package playerService.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import playerService.dto.LoginDto;
import playerService.dto.RegisterDto;
import playerService.model.Player;
import playerService.model.Session;
import playerService.repository.PlayerRepository;
import playerService.repository.SessionRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PlayerService {
    @Autowired
    private PlayerRepository playerRepo;
    @Autowired private SessionRepository sessionRepo;

    public Player register(RegisterDto dto) {
        Player player = new Player();
        player.setEmail(dto.email);
        player.setPassword(new BCryptPasswordEncoder().encode(dto.password));
        player.setName(dto.name);
        player.setSurname(dto.surname);
        player.setDateOfBirth(dto.dateOfBirth);
        player.setAddress(dto.address);
        return playerRepo.save(player);
    }

    public String login(LoginDto dto) {
        Player player = playerRepo.findByEmail(dto.email).orElseThrow();
        if (!new BCryptPasswordEncoder().matches(dto.password, player.getPassword())) throw new RuntimeException("Invalid credentials");
        if (player.getDailyLimit() != null && player.getUsedToday().compareTo(player.getDailyLimit()) >= 0) throw new RuntimeException("Time limit reached");

        Session session = new Session();
        session.setSessionId(UUID.randomUUID().toString());
        session.setPlayerId(player.getId());
        session.setLoginTime(LocalDateTime.now());
        sessionRepo.save(session);

        player.setActive(true);
        playerRepo.save(player);
        return session.getSessionId();
    }

    public void logout(String sessionId) {
        Session s = sessionRepo.findById(sessionId).orElseThrow();
        s.setLogoutTime(LocalDateTime.now());
        sessionRepo.save(s);

        Player player = playerRepo.findById(s.getPlayerId()).orElseThrow();
        Duration sessionDuration = Duration.between(s.getLoginTime(), s.getLogoutTime());
        player.setUsedToday(player.getUsedToday().plus(sessionDuration));
        player.setActive(false);
        playerRepo.save(player);
    }

    public void setTimeLimit(Long playerId, long minutes) {
        Player player = playerRepo.findById(playerId).orElseThrow();
        if (!player.isActive()) throw new RuntimeException("Player must be active");
        player.setDailyLimit(Duration.ofMinutes(minutes));
        playerRepo.save(player);
    }
    public void deleteAllUsers(){
        playerRepo.deleteAll();
    }
}

