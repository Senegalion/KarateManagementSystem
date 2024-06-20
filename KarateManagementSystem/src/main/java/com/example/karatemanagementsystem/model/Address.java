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
@Table(name = "addresses")
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    long id;

    private String city;
    private String street;
    private String number;
    private String postalCode;

    @JsonManagedReference
    @JsonIgnore
    @OneToMany(mappedBy = "address", fetch = FetchType.EAGER)
    private List<User> userList;

}
