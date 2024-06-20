import {Address} from "../addresses/address.model";
import {KarateClubName} from "../models/karate-club-name.model";
import {KarateRank} from "../models/karate-rank.model";

export class SignupInfo {
  name: string;
  surname: string;
  dateOfBirth: string;
  address: Address;
  pesel: string;
  karateClubName: KarateClubName;
  karateRank: KarateRank;
  email: string;
  role: string[];
  password: string;

  constructor(
    name: string,
    surname: string,
    dateOfBirth: string,
    address: Address,
    pesel: string,
    karateClubName: KarateClubName,
    karateRank: KarateRank,
    email: string,
    password: string
  ) {
    this.name = name;
    this.surname = surname;
    this.dateOfBirth = dateOfBirth;
    this.address = address;
    this.pesel = pesel;
    this.karateClubName = karateClubName;
    this.karateRank = karateRank;
    this.email = email;
    this.role = ['user'];
    this.password = password;
  }
}
