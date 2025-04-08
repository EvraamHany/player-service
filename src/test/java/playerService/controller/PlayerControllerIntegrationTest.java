package playerService.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import playerService.Application;
import playerService.dto.LoginDto;
import playerService.dto.RegisterDto;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;



@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class PlayerControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @Test
    public void testRegisterAndLoginIntegration() throws Exception {
        RegisterDto registerDto = new RegisterDto();
        registerDto.email = "int@example.com";
        registerDto.password = "password";
        registerDto.name = "Int";
        registerDto.surname = "User";
        registerDto.dateOfBirth = LocalDate.of(1995, 5, 5);
        registerDto.address = "Anywhere";

        mockMvc.perform(post("/api/players/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("int@example.com"));

        LoginDto loginDto = new LoginDto();
        loginDto.email = "int@example.com";
        loginDto.password = "password";

        MvcResult result = mockMvc.perform(post("/api/players/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isOk())
                .andReturn();

        String sessionId = result.getResponse().getContentAsString();

        mockMvc.perform(post("/api/players/logout")
                        .header("Session-ID", sessionId))
                .andExpect(status().isOk());
    }
}
