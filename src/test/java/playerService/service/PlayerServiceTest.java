package playerService.service;

import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import playerService.Application;
import playerService.dto.LoginDto;
import playerService.dto.RegisterDto;
import playerService.model.Player;
import playerService.repository.PlayerRepository;
import playerService.service.PlayerService;

import java.time.LocalDate;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class PlayerServiceTest {

    @Autowired
    private PlayerService service;
    @Autowired private PlayerRepository playerRepo;

    @BeforeEach
    void setup() {
        playerRepo.deleteAll();
    }

    @Test
    public void testRegisterAndLoginFlow() {
        service.deleteAllUsers();
        RegisterDto register = new RegisterDto();
        register.email = "test@example.com";
        register.password = "password";
        register.name = "John";
        register.surname = "Doe";
        register.dateOfBirth = LocalDate.of(1990, 1, 1);
        register.address = "123 Street";

        Player player = service.register(register);
        assertNotNull(player.getId());

        LoginDto login = new LoginDto();
        login.email = "test@example.com";
        login.password = "password";

        String sessionId = service.login(login);
        assertNotNull(sessionId);

        service.logout(sessionId);
    }

    @Test
    public void testSetLimitFailsWhenInactive() {
        RegisterDto register = new RegisterDto();
        register.email = "inactive@example.com";
        register.password = "password";
        register.name = "Inactive";
        register.surname = "User";
        register.dateOfBirth = LocalDate.of(1990, 1, 1);
        register.address = "Nowhere";

        Player player = service.register(register);

        assertThrows(RuntimeException.class, () -> {
            service.setTimeLimit(player.getId(), 60);
        });
    }
}
