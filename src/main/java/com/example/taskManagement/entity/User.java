package com.example.taskManagement.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Entity
@Table(name = "users")  // Mapping to the 'users' table
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "First name cannot be null")
    private String firstName;

    @NotNull(message = "Last name cannot be null")
    private String lastName;

    @Email(message = "Email should be valid")
    private String email;

    @NotNull(message = "Role cannot be null")
    private String password;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Role cannot be null")
    private Role role;

    public enum Role {
        USER, IT_TEAM, IT_MANAGER, ADMIN, IT_DIRECTOR
    }

    @Enumerated(EnumType.STRING)
    private RequestType requestType;
}
