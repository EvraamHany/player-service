package playerService.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import playerService.dto.LimitDto;
import playerService.dto.LoginDto;
import playerService.dto.RegisterDto;
import playerService.model.Player;
import playerService.service.PlayerService;

@RestController
@RequestMapping("/api/players")
public class PlayerController {
    @Autowired
    private PlayerService service;

    @PostMapping("/register")
    public ResponseEntity<Player> register(@RequestBody RegisterDto dto) {
        return ResponseEntity.ok(service.register(dto));
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginDto dto) {
        return ResponseEntity.ok(service.login(dto));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Session-ID") String sessionId) {
        service.logout(sessionId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/limit")
    public ResponseEntity<Void> setLimit(@PathVariable Long id, @RequestBody LimitDto dto) {
        service.setTimeLimit(id, dto.minutes);
        return ResponseEntity.ok().build();
    }
}

