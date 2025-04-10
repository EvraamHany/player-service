package playerService.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import playerService.dto.LoginRequestDto;
import playerService.dto.SessionResponseDto;
import playerService.service.SessionService;

@RestController
@RequestMapping("/api/sessions")
public class SessionController {

    private final SessionService sessionService;

    @Autowired
    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @PostMapping("/login")
    public ResponseEntity<SessionResponseDto> login(@Valid @RequestBody LoginRequestDto loginRequest) {
        SessionResponseDto sessionResponse = sessionService.login(loginRequest);
        return new ResponseEntity<>(sessionResponse, HttpStatus.OK);
    }

    @PostMapping("/logout/{sessionId}")
    public ResponseEntity<Void> logout(@PathVariable String sessionId) {
        sessionService.logout(sessionId);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
