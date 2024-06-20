import {Injectable} from '@angular/core';
import {ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot, UrlTree} from '@angular/router';
import {Observable} from 'rxjs';
import {TokenStorageService} from "../auth/token-storage.service";

@Injectable({
  providedIn: 'root'
})
export class RoleGuard implements CanActivate {

  constructor(private tokenStorageService: TokenStorageService, private router: Router) {
  }

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot): Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {

    // check if any role from authorities list is in the routing list defined
    for (let i = 0; i < route.data['roles'].length; i++) {
      for (let j = 0; j < this.tokenStorageService.getAuthorities().length; j++) {
        if (route.data['roles'][i] === this.tokenStorageService.getAuthorities()[j]) {
          return true;
        }
      }
    }

    // otherwise redirect to specified url
    this.router.navigateByUrl('').then();
    return false;
  }
}
