import {Component, OnInit} from '@angular/core';
import {TrainingSession} from "./training-session.model";
import {DatePipe, NgClass, NgForOf, NgIf} from "@angular/common";
import {TrainingSessionService} from "./training-session.service";
import {Feedback} from "../feedback/feedback.model";

@Component({
  selector: 'app-training-sessions',
  templateUrl: './training-sessions.component.html',
  standalone: true,
  imports: [
    NgForOf,
    NgIf,
    NgClass,
    DatePipe
  ],
  styleUrls: ['./training-sessions.component.css']
})
export class TrainingSessionsComponent implements OnInit {
  trainingSessions: TrainingSession[] = [];
  signedUpSessions: TrainingSession[] = [];
  selectedSession: TrainingSession | null = null;
  feedbacks: { [key: number]: Feedback[] } = {};
  days = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];
  calendar: any[] = [];

  constructor(private trainingSessionService: TrainingSessionService) {}

  ngOnInit(): void {
    this.loadSessions();
  }

  loadSessions() {
    this.trainingSessionService.getTrainingSessions().subscribe(sessions => {
      this.trainingSessions = sessions;
      this.generateCalendar();
      this.loadUserSignedUpSessions();
    });
  }

  loadUserSignedUpSessions() {
    this.trainingSessionService.getUserSignedUpSessions().subscribe(sessions => {
      this.signedUpSessions = sessions;
    });
  }

  isUserSignedUp(sessionId: number): boolean {
    return this.signedUpSessions.some(session => session.id === sessionId);
  }

  hasUserFeedback(sessionId: number): boolean {
    return this.feedbacks[sessionId]?.length > 0;
  }

  signUp(sessionId: number) {
    this.trainingSessionService.signUpForTraining(sessionId).subscribe(() => {
      alert('Signed up successfully!');
      this.loadUserSignedUpSessions();
    });
  }

  withdraw(sessionId: number) {
    this.trainingSessionService.withdrawFromSession(sessionId).subscribe(() => {
      alert('Withdrawn from session successfully!');
      this.loadUserSignedUpSessions();
    });
  }

  getStars(starRating: number): number[] {
    return Array(starRating).fill(0).map((x, i) => i);
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
