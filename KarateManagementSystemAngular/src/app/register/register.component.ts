import {Component, OnInit} from '@angular/core';
import {AuthService} from '../auth/auth.service';
import {SignupInfo} from '../auth/signup-info';
import {FormsModule, NgForm} from '@angular/forms';
import {NgForOf, NgIf} from "@angular/common";
import {Address} from "../addresses/address.model";
import {KarateClubName} from "../models/karate-club-name.model";
import {KarateRank} from "../models/karate-rank.model";
import {Router} from "@angular/router";

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  standalone: true,
  imports: [
    FormsModule,
    NgIf,
    NgForOf
  ],
  styleUrls: ['./register.component.css']
})
export class RegisterComponent implements OnInit {
  form: any = {};
  signupInfo?: SignupInfo;
  isSuccessful = false;
  isSignedUp = false;
  isSignUpFailed = false;
  errorMessage = '';
  karateClubs = Object.values(KarateClubName);
  karateRanks = Object.values(KarateRank);

  constructor(private authService: AuthService, private router: Router) {}

  ngOnInit(): void {}

  onSubmit(registerForm: NgForm) {
    const address = new Address(
      this.form.city,
      this.form.street,
      this.form.number,
      this.form.postalCode
    );

    this.signupInfo = new SignupInfo(
      this.form.name,
      this.form.surname,
      this.form.dateOfBirth,
      address,
      this.form.pesel,
      this.form.karateClubName,
      this.form.karateRank,
      this.form.email,
      this.form.password
    );

    this.authService.signUp(this.signupInfo).subscribe({
      next: (data) => {
        console.log(data);
        this.isSuccessful = true;
        this.isSignUpFailed = false;
        setTimeout(() => {
          this.router.navigate(['/home']);
        }, 3000); // Redirect after 3 seconds
      },
      error: (error) => {
        console.log(error);
        this.errorMessage = error.error.message;
        this.isSignUpFailed = true;
      }
    });
  }
}
