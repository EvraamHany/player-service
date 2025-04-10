package playerService.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SessionResponseDto {
    private String sessionId;
    private String playerEmail;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
}
