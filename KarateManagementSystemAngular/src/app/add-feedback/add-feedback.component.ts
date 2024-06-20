import { Component } from '@angular/core';
import {FormsModule} from "@angular/forms";
import {Feedback} from "../feedback/feedback.model";
import {HttpClient} from "@angular/common/http";

@Component({
  selector: 'app-add-feedback',
  standalone: true,
  imports: [
    FormsModule
  ],
  templateUrl: './add-feedback.component.html',
  styleUrl: './add-feedback.component.css'
})
export class AddFeedbackComponent {
  sessionId: number | undefined;
  userId: number | undefined;
  feedback: Feedback = new Feedback();

  constructor(private http: HttpClient) {}

  onSubmit() {
    if (this.sessionId !== undefined && this.userId !== undefined) {
      this.http.post<Feedback>(`http://localhost:8080/admin/trainings/${this.sessionId}/feedback?userId=${this.userId}`, this.feedback).subscribe(feedback => {
        console.log('Feedback added', feedback);
      });
    } else {
      console.error("Session ID or User ID is not defined.");
    }
  }
}
