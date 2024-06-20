import {Component, OnInit} from '@angular/core';
import {FormsModule} from "@angular/forms";
import {TrainingSession} from "../training-sessions/training-session.model";
import {HttpClient} from "@angular/common/http";
import {TrainingSessionService} from "../training-sessions/training-session.service";
import {DatePipe, NgClass, NgForOf, NgIf} from "@angular/common";
import {Feedback} from "../feedback/feedback.model";
import {User} from "../user/user.model";

@Component({
  selector: 'app-add-training',
  standalone: true,
  imports: [
    FormsModule,
    NgForOf,
    NgIf,
    NgClass,
    DatePipe
  ],
  templateUrl: './add-training.component.html',
  styleUrl: './add-training.component.css'
})
export class AddTrainingComponent implements OnInit {
  trainingSession: TrainingSession = new TrainingSession();
  trainingSessions: TrainingSession[] = [];
  selectedSession: TrainingSession | null = null;
  selectedUser: User | null = null;
  feedback: Feedback = new Feedback();
  days = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];
  calendar: any[] = [];

  constructor(private http: HttpClient, private trainingSessionService: TrainingSessionService) {}

  ngOnInit(): void {
    this.loadTrainingSessions();
  }

  loadTrainingSessions() {
    this.trainingSessionService.getTrainingSessions().subscribe(sessions => {
      this.trainingSessions = sessions;
      this.generateCalendar();
    });
  }

  onSubmit() {
    if (this.trainingSession.date && this.trainingSession.description) {
      this.http.post<TrainingSession>('http://localhost:8080/admin/trainings', this.trainingSession).subscribe(session => {
        console.log('Training session added', session);
        this.loadTrainingSessions();
      });
    }
  }

  deleteTrainingSession(sessionId: number) {
    this.trainingSessionService.deleteTrainingSession(sessionId).subscribe(() => {
      this.loadTrainingSessions();
    });
  }

  viewMembers(session: TrainingSession) {
    this.trainingSessionService.getTrainingSessionMembers(session.id!).subscribe(users => {
      session.users = users;
      this.selectedSession = session;
      this.selectedUser = null;
    });
  }

  selectUser(user: User) {
    this.selectedUser = user;
  }

  addFeedback() {
    if (this.selectedSession && this.selectedUser) {
      this.trainingSessionService.addFeedback(this.selectedSession.id!, this.selectedUser.id!, this.feedback).subscribe(users => {
        console.log('Feedback added');
        this.feedback = new Feedback();
        this.selectedSession!.users = users;
        this.selectedUser = null;
      });
    }
  }

  generateCalendar() {
    const startOfMonth = new Date(new Date().getFullYear(), new Date().getMonth(), 1);
    const endOfMonth = new Date(new Date().getFullYear(), new Date().getMonth() + 1, 0);
    const calendar = [];
    let week = [];

    for (let i = 0; i < startOfMonth.getDay(); i++) {
      week.push({date: null, sessions: []});
    }

    for (let day = 1; day <= endOfMonth.getDate(); day++) {
      const date = new Date(startOfMonth.getFullYear(), startOfMonth.getMonth(), day);
      const sessions = this.trainingSessions.filter(session => {
        const sessionDate = new Date(session.date);
        return sessionDate.getDate() === date.getDate() &&
          sessionDate.getMonth() === date.getMonth() &&
          sessionDate.getFullYear() === date.getFullYear();
      });

      week.push({date, sessions});

      if (week.length === 7) {
        calendar.push(week);
        week = [];
      }
    }

    while (week.length < 7) {
      week.push({date: null, sessions: []});
    }

    calendar.push(week);
    this.calendar = calendar;
  }

  isToday(date: Date | null): boolean {
    if (!date) return false;
    const today = new Date();
    return date.getDate() === today.getDate() &&
      date.getMonth() === today.getMonth() &&
      date.getFullYear() === today.getFullYear();
  }
}
