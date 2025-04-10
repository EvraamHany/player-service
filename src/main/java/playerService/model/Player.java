package playerService.model;

import java.time.LocalDate;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Player {
    @Id
    @GeneratedValue
    private Long id;

    @Email
    @Column(unique = true, nullable = false)
    private String email;

    @NotBlank
    private String password;

    @NotBlank
    private String name;

    @NotBlank
    private String surname;

    @NotNull
    @Past
    private LocalDate dateOfBirth;

    @NotBlank
    private String address;

    private Integer dailyTimeLimit;

    @Column(columnDefinition = "boolean default false")
    private boolean active = false;

    @Column
    private LocalDateTime lastSessionStart;

    @Column
    private Long todaySessionTime = 0L;

    @Column
    private LocalDateTime lastDailyReset;
}