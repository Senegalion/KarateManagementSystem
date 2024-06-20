package com.example.karatemanagementsystem.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "karate_clubs")
public class KarateClub {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private KarateClubName name;

    @JsonManagedReference
    @JsonIgnore
    @OneToMany(mappedBy = "karateClub", fetch = FetchType.EAGER)
    private List<User> users;
}