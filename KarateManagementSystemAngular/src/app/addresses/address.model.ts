export class Address {
  id?: number;
  city: string;
  street: string;
  number: string;
  postalCode: string;

  constructor(city: string, street: string, number: string, postalCode: string) {
    this.city = city;
    this.street = street;
    this.number = number;
    this.postalCode = postalCode;
  }
}
