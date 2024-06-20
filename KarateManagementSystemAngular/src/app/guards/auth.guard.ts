import {CanActivateFn, Router} from '@angular/router';
import {TokenStorageService} from "../auth/token-storage.service";
import {inject} from "@angular/core";

export const authGuard: CanActivateFn = (route, state) => {

  const tokenStorageService = inject(TokenStorageService);
  const router = inject(Router);

  // check if any role from authorities list is in the routing list defined
  for (let i = 0; i < route.data['roles'].length; i++) {
    for (let j = 0; j < tokenStorageService.getAuthorities().length; j++) {
      if (route.data['roles'][i] === tokenStorageService.getAuthorities()[j]) {
        return true;
      }
    }
  }

  // otherwise redirect to specified url
  // this.router.navigateByUrl('').then();
  return router.parseUrl('');
};
