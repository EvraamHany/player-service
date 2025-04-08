package playerService.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.time.Duration;
import java.time.LocalDate;

@Entity
public class Player {
    @Id
    @GeneratedValue
    private Long id;
    private String email;
    private String password;
    private String name;
    private String surname;
    private LocalDate dateOfBirth;
    private String address;
    private Duration dailyLimit;
    private Duration usedToday;
    private boolean active;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Duration getDailyLimit() {
        return dailyLimit;
    }

    public void setDailyLimit(Duration dailyLimit) {
        this.dailyLimit = dailyLimit;
    }

    public Duration getUsedToday() {
        return usedToday;
    }

    public void setUsedToday(Duration usedToday) {
        this.usedToday = usedToday;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}

