import {Injectable} from "@angular/core";
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {Observable} from "rxjs";
import {TrainingSession} from "./training-session.model";
import {Feedback} from "../feedback/feedback.model";
import {User} from "../user/user.model";

const httpOptions = {
  headers: new HttpHeaders({'Content-Type': 'application/json'})
};

@Injectable({
  providedIn: 'root'
})
export class TrainingSessionService {
  private baseUrl = 'http://localhost:8080';
  private userTrainingsUrl = 'http://localhost:8080/users/trainings';
  private adminTrainingsUrl = 'http://localhost:8080/admin/trainings';

  constructor(private http: HttpClient) {}

  getTrainingSessions(): Observable<TrainingSession[]> {
    return this.http.get<TrainingSession[]>(`${this.baseUrl}/trainings`);
  }

  signUpForTraining(sessionId: number): Observable<any> {
    return this.http.post(`${this.userTrainingsUrl}/${sessionId}/signup`, {}, httpOptions);
  }

  getFeedback(sessionId: number): Observable<Feedback[]> {
    return this.http.get<Feedback[]>(`${this.userTrainingsUrl}/${sessionId}/feedback`);
  }

  deleteTrainingSession(sessionId: number): Observable<any> {
    return this.http.delete(`${this.adminTrainingsUrl}/${sessionId}`, httpOptions);
  }

  getUserSignedUpSessions(): Observable<TrainingSession[]> {
    return this.http.get<TrainingSession[]>(`${this.baseUrl}/users/signed-up-sessions`);
  }

  withdrawFromSession(sessionId: number): Observable<any> {
    return this.http.post(`${this.userTrainingsUrl}/${sessionId}/withdraw`, {}, httpOptions);
  }

  getTrainingSessionMembers(sessionId: number): Observable<User[]> {
    return this.http.get<User[]>(`${this.adminTrainingsUrl}/${sessionId}/members`, httpOptions);
  }

  addFeedback(sessionId: number, userId: number, feedback: Feedback): Observable<User[]> {
    return this.http.post<User[]>(`${this.adminTrainingsUrl}/${sessionId}/feedback?userId=${userId}`, feedback, httpOptions);
  }
}
