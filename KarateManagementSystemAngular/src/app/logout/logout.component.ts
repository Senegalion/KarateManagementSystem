import { Component } from '@angular/core';
import {NgIf} from "@angular/common";
import {TokenStorageService} from "../auth/token-storage.service";

@Component({
  selector: 'app-logout',
  standalone: true,
    imports: [
        NgIf
    ],
  templateUrl: './logout.component.html',
  styleUrl: './logout.component.css'
})
export class LogoutComponent {
  info: any;

  constructor(private token: TokenStorageService) { }

  ngOnInit() {
    this.info = {
      token: this.token.getToken(),
      username: this.token.getUsername(),
      authorities: this.token.getAuthorities()
    };
  }

  logout() {
    this.token.signOut();
    window.location.reload();
  }
}
