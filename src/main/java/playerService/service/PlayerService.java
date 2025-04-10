package playerService.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import playerService.dto.PlayerRegistrationDto;
import playerService.dto.TimeLimitDto;
import playerService.exception.PlayerAlreadyExistsException;
import playerService.exception.PlayerInactiveException;
import playerService.exception.PlayerNotFoundException;
import playerService.model.Player;
import playerService.repository.PlayerRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class PlayerService {

    private final PlayerRepository playerRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public PlayerService(PlayerRepository playerRepository, PasswordEncoder passwordEncoder) {
        this.playerRepository = playerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Player registerPlayer(PlayerRegistrationDto registrationDto) {
        if (playerRepository.existsByEmail(registrationDto.getEmail())) {
            throw new PlayerAlreadyExistsException("Player with email " + registrationDto.getEmail() + " already exists");
        }

        Player player = new Player();
        player.setEmail(registrationDto.getEmail());
        player.setPassword(passwordEncoder.encode(registrationDto.getPassword()));
        player.setName(registrationDto.getName());
        player.setSurname(registrationDto.getSurname());
        player.setDateOfBirth(registrationDto.getDateOfBirth());
        player.setAddress(registrationDto.getAddress());
        player.setActive(true);
        player.setLastDailyReset(LocalDateTime.now());

        return playerRepository.save(player);
    }

    @Transactional
    public Player getPlayerById(Long id) {
        return playerRepository.findById(id)
                .orElseThrow(() -> new PlayerNotFoundException("Player not found with id: " + id));
    }

    @Transactional
    public Player getPlayerByEmail(String email) {
        return playerRepository.findByEmail(email)
                .orElseThrow(() -> new PlayerNotFoundException("Player not found with email: " + email));
    }

    @Transactional
    public Player setTimeLimit(TimeLimitDto timeLimitDto) {
        Player player = getPlayerById(timeLimitDto.getPlayerId());

        if (!player.isActive()) {
            throw new PlayerInactiveException("Cannot set time limit for inactive player");
        }

        player.setDailyTimeLimit(timeLimitDto.getDailyLimitMinutes());
        return playerRepository.save(player);
    }

    @Transactional
    public void updatePlayerSessionTime(Player player, long sessionTimeSeconds) {
        // Reset daily counter if it's a new day
        if (player.getLastDailyReset() != null &&
                !player.getLastDailyReset().toLocalDate().equals(LocalDate.now())) {
            player.setTodaySessionTime(0L);
            player.setLastDailyReset(LocalDateTime.now());
        }

        player.setTodaySessionTime(player.getTodaySessionTime() + sessionTimeSeconds);
        playerRepository.save(player);
    }

    @Transactional
    public boolean hasExceededTimeLimit(Player player) {
        if (player.getDailyTimeLimit() == null) {
            return false;
        }

        if (player.getLastDailyReset() != null &&
                !player.getLastDailyReset().toLocalDate().equals(LocalDate.now())) {
            player.setTodaySessionTime(0L);
            player.setLastDailyReset(LocalDateTime.now());
            playerRepository.save(player);
            return false;
        }

        return player.getTodaySessionTime() >= player.getDailyTimeLimit() * 60;
    }

    @Transactional
    public void updateSessionStartTime(Player player) {
        player.setLastSessionStart(LocalDateTime.now());
        playerRepository.save(player);
    }
}