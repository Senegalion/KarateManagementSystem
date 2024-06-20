import { TrainingSession } from "../training-sessions/training-session.model";
import { User } from "../user/user.model";

export class Feedback {
  id?: number;
  user: User;
  trainingSession: TrainingSession;
  comment: string;
  starRating: number;

  constructor(user?: User, trainingSession?: TrainingSession, comment?: string, starRating?: number) {
    this.user = user || new User();
    this.trainingSession = trainingSession || new TrainingSession();
    this.comment = comment || '';
    this.starRating = starRating || 1;
  }
}
