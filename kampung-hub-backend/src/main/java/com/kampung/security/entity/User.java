package com.kampung.security.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @Column(name = "user_id", nullable = false, updatable = false)
    private String userId;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "password")
    private String password;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
