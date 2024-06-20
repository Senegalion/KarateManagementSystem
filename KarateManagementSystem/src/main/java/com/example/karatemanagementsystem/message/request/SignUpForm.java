package com.example.karatemanagementsystem.message.request;

import com.example.karatemanagementsystem.model.Address;
import com.example.karatemanagementsystem.model.KarateClub;
import com.example.karatemanagementsystem.model.KarateClubName;
import com.example.karatemanagementsystem.model.KarateRank;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class SignUpForm {
    @NotBlank
    @Size(min = 3, max = 50)
    private String name;

    @NotBlank
    @Size(min = 3, max = 50)
    private String surname;

    @NotBlank
    private String dateOfBirth;

    private Address address;

    @Size(min = 11, max = 11)
    private String pesel;

    private KarateClubName karateClubName;

    private KarateRank karateRank;

    @Email
    private String email;

    private Set<String> role;

    @NotBlank
    @Size(min = 6, max = 40)
    private String password;
}
