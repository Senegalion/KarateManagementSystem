import {Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class FeedbackService {
  private feedbackUrl = 'http://localhost:8080/users/trainings';

  constructor(private http: HttpClient) {}

  getFeedbackForTraining(sessionId: number): Observable<any> {
    return this.http.get(`${this.feedbackUrl}/${sessionId}/feedback`);
  }
}
