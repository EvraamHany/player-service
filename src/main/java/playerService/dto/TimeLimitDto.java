package playerService.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;



public class TimeLimitDto {
    @NotNull
    private Long playerId;

    @NotNull
    @Positive
    private Integer dailyLimitMinutes;

    public TimeLimitDto() {
    }
    public TimeLimitDto(@NotNull Long playerId, @NotNull @Positive Integer dailyLimitMinutes) {
        this.playerId = playerId;
        this.dailyLimitMinutes = dailyLimitMinutes;
    }

    public @NotNull Long getPlayerId() {
        return playerId;
    }

    public void setPlayerId(@NotNull Long playerId) {
        this.playerId = playerId;
    }

    public @NotNull @Positive Integer getDailyLimitMinutes() {
        return dailyLimitMinutes;
    }

    public void setDailyLimitMinutes(@NotNull @Positive Integer dailyLimitMinutes) {
        this.dailyLimitMinutes = dailyLimitMinutes;
    }
}