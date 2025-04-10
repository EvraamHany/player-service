package playerService.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public @Email String getEmail() {
        return email;
    }

    public void setEmail(@Email String email) {
        this.email = email;
    }

    public @NotBlank String getPassword() {
        return password;
    }

    public void setPassword(@NotBlank String password) {
        this.password = password;
    }

    public @NotBlank String getName() {
        return name;
    }

    public void setName(@NotBlank String name) {
        this.name = name;
    }

    public @NotBlank String getSurname() {
        return surname;
    }

    public void setSurname(@NotBlank String surname) {
        this.surname = surname;
    }

    public @NotNull @Past LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(@NotNull @Past LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public @NotBlank String getAddress() {
        return address;
    }

    public void setAddress(@NotBlank String address) {
        this.address = address;
    }

    public Integer getDailyTimeLimit() {
        return dailyTimeLimit;
    }

    public void setDailyTimeLimit(Integer dailyTimeLimit) {
        this.dailyTimeLimit = dailyTimeLimit;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getLastSessionStart() {
        return lastSessionStart;
    }

    public void setLastSessionStart(LocalDateTime lastSessionStart) {
        this.lastSessionStart = lastSessionStart;
    }

    public Long getTodaySessionTime() {
        return todaySessionTime;
    }

    public void setTodaySessionTime(Long todaySessionTime) {
        this.todaySessionTime = todaySessionTime;
    }

    public LocalDateTime getLastDailyReset() {
        return lastDailyReset;
    }

    public void setLastDailyReset(LocalDateTime lastDailyReset) {
        this.lastDailyReset = lastDailyReset;
    }
}