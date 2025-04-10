package playerService.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimeLimitDto {
    @NotNull
    private Long playerId;

    @NotNull
    @Positive
    private Integer dailyLimitMinutes;
}