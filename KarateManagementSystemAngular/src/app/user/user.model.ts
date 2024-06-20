import { TrainingSession } from "../training-sessions/training-session.model";
import { Address } from "../addresses/address.model";
import { KarateClubName } from "../models/karate-club-name.model";
import { KarateRank } from "../models/karate-rank.model";
import { Feedback } from "../feedback/feedback.model";

export class User {
  id?: number;
  name: string;
  surname: string;
  dateOfBirth: string;
  address: Address;
  pesel: string;
  karateClubName: KarateClubName;
  karateRank: KarateRank;
  trainingSessions: TrainingSession[] = [];
  feedbacks: Feedback[] = [];
  email: string;
  password: string;
  roles: Role[];

  constructor(
    name: string = '',
    surname: string = '',
    dateOfBirth: string = '',
    address: Address = new Address('', '', '', ''),
    pesel: string = '',
    karateClubName: KarateClubName = KarateClubName.LODZKIE_CENTRUM_OKINAWA_SHORIN_RYU_KARATE_I_KOBUDO,
    karateRank: KarateRank = KarateRank.KYU_10,
    trainingSessions: TrainingSession[] = [],
    feedbacks: Feedback[] = [],
    email: string = '',
    password: string = '',
    roles: Role[] = []
  ) {
    this.name = name;
    this.surname = surname;
    this.dateOfBirth = dateOfBirth;
    this.address = address;
    this.pesel = pesel;
    this.karateClubName = karateClubName;
    this.karateRank = karateRank;
    this.trainingSessions = trainingSessions;
    this.feedbacks = feedbacks;
    this.email = email;
    this.password = password;
    this.roles = roles;
  }
}

export class Role {
  id: number;
  name: string;

  constructor(id: number = 0, name: string = '') {
    this.id = id;
    this.name = name;
  }
}
