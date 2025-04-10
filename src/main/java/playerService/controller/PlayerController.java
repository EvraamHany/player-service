package playerService.controller;


import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import playerService.dto.PlayerRegistrationDto;
import playerService.dto.TimeLimitDto;
import playerService.model.Player;
import playerService.service.PlayerService;

@RestController
@RequestMapping("/api/players")
public class PlayerController {

    private final PlayerService playerService;

    @Autowired
    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @PostMapping("/register")
    public ResponseEntity<Player> registerPlayer(@Valid @RequestBody PlayerRegistrationDto registrationDto) {
        Player registeredPlayer = playerService.registerPlayer(registrationDto);
        return new ResponseEntity<>(registeredPlayer, HttpStatus.CREATED);
    }

    @PostMapping("/time-limit")
    public ResponseEntity<Player> setTimeLimit(@Valid @RequestBody TimeLimitDto timeLimitDto) {
        Player player = playerService.setTimeLimit(timeLimitDto);
        return new ResponseEntity<>(player, HttpStatus.OK);
    }
}
