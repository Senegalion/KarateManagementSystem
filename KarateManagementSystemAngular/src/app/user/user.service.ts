import {Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {User} from "./user.model";

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private adminUrl = 'http://localhost:8080/users';

  constructor(private http: HttpClient) {}

  getAllUsers(): Observable<any> {
    return this.http.get(`${this.adminUrl}/users`);
  }

  deleteUser(userId: number): Observable<any> {
    return this.http.delete(`${this.adminUrl}/users/${userId}`);
  }

  getUserInfo(): Observable<User> {
    return this.http.get<User>(`${this.adminUrl}/me`); // Assuming the endpoint to get the logged-in user's info
  }
}
