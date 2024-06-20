package com.example.karatemanagementsystem.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Data
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(min = 3, max = 50)
    private String name;

    @NotBlank
    @Size(min = 3, max = 50)
    private String surname;

    @NotBlank
    private String dateOfBirth;

    @JsonIgnore
    @ManyToOne(cascade = CascadeType.MERGE)
    private Address address;

    @Size(min = 11, max = 11)
    private String pesel;

    @JsonIgnore
    @ManyToOne(cascade = CascadeType.MERGE)
    private KarateClub karateClub;

    private KarateRank karateRank;

    @JsonIgnore
    @ManyToMany(mappedBy = "users")
    private List<TrainingSession> trainingSessions;

    @JsonManagedReference(value = "userReference")
    @OneToMany(mappedBy = "user")
    private Set<Feedback> feedbacks = new HashSet<>();

    @Email
    private String email;

    @NotBlank
    @Size(min = 6, max = 100)
    private String password;

    @ManyToMany(fetch = FetchType.EAGER)
    private Set<Role> roles = new HashSet<>();

    public User() {
    }

    public User(String name, String surname, String dateOfBirth, String pesel, KarateRank karateRank, String email, String password) {
        this.name = name;
        this.surname = surname;
        this.dateOfBirth = dateOfBirth;
        this.pesel = pesel;
        this.karateRank = karateRank;
        this.email = email;
        this.password = password;
    }
}
