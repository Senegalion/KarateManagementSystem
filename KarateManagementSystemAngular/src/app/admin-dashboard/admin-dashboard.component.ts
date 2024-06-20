import {Component, OnInit} from '@angular/core';
import {UserService} from "../user/user.service";
import {NgForOf, NgIf} from "@angular/common";
import {AdminService} from "../admin/admin.service";
import {HttpClient} from "@angular/common/http";

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [
    NgForOf,
    NgIf
  ],
  templateUrl: './admin-dashboard.component.html',
  styleUrl: './admin-dashboard.component.css'
})
export class AdminDashboardComponent implements OnInit {
  users: any[] = [];
  errorMessage: string = '';

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.loadUsers();
  }

  loadUsers() {
    this.http.get<any[]>('http://localhost:8080/admin/users').subscribe({
      next: (data) => this.users = data,
      error: (error) => this.errorMessage = error.message
    });
  }

  loadUsersByRole() {
    this.http.get<any[]>('http://localhost:8080/admin/users/by-role').subscribe({
      next: (data) => this.users = data,
      error: (error) => this.errorMessage = error.message
    });
  }

  loadUsersByRank() {
    this.http.get<any[]>('http://localhost:8080/admin/users/by-rank').subscribe({
      next: (data) => this.users = data,
      error: (error) => this.errorMessage = error.message
    });
  }

  loadUsersAlphabetically() {
    this.users.sort((a, b) => a.name.localeCompare(b.name));
  }

  deleteUser(userId: number) {
    this.http.delete(`http://localhost:8080/admin/users/${userId}`).subscribe({
      next: () => this.loadUsers(),
      error: (error) => this.errorMessage = error.message
    });
  }

  getUserRoles(user: any): string {
    return user.roles.map((role: any) => role.name).join(', ');
  }
}
