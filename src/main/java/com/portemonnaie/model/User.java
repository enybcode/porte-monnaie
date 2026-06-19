package com.portemonnaie.model;

import java.time.LocalDate;

public class User {
    private final int id;
    private final String firstName;
    private final String lastName;
    private final String email;
    private final LocalDate createdAt;

    public User(int id, String firstName, String lastName, String email, LocalDate createdAt) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public LocalDate getCreatedAt() { return createdAt; }

    public String getDisplayName() {
        String fullName = (firstName + " " + lastName).trim();
        return fullName.isBlank() ? email : fullName;
    }
}
