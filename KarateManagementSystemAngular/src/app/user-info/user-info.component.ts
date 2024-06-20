import {Component, OnInit} from '@angular/core';
import {User} from "../user/user.model";
import {UserService} from "../user/user.service";
import {NgIf} from "@angular/common";
import {HttpClient} from "@angular/common/http";

@Component({
  selector: 'app-user-info',
  standalone: true,
  imports: [
    NgIf
  ],
  templateUrl: './user-info.component.html',
  styleUrl: './user-info.component.css'
})
export class UserInfoComponent implements OnInit {
  user: any;
  errorMessage: string = '';

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.loadUserInfo();
  }

  loadUserInfo() {
    this.http.get<any>('http://localhost:8080/users/me').subscribe({
      next: (data) => this.user = data,
      error: (error) => this.errorMessage = error.message
    });
  }
}
