import { Feedback } from "../feedback/feedback.model";
import { User } from "../user/user.model";

export class TrainingSession {
  id?: number;
  date: string;
  description: string;
  users: User[] = [];
  feedbacks: Feedback[] = [];

  constructor(date?: string, description?: string) {
    this.date = date || '';
    this.description = description || '';
  }
}
