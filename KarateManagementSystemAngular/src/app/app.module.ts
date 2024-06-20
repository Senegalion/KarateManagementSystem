import {BrowserModule} from '@angular/platform-browser';
import {NgModule} from '@angular/core';
import {HttpClientModule} from '@angular/common/http';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {RouterModule, Routes} from '@angular/router';

import {AppComponent} from './app.component';
import {HomeComponent} from './home/home.component';
import {RegisterComponent} from './register/register.component';
import {LoginComponent} from './login/login.component';
import {AdminComponent} from './admin/admin.component';
import {UserComponent} from './user/user.component';

import {httpInterceptorProviders} from './auth/auth-interceptor';
import {authGuard} from './guards/auth.guard';
import {RoleGuard} from './guards/role.guard';
import {TrainingSessionsComponent} from "./training-sessions/training-sessions.component";
import {UserDashboardComponent} from "./user-dashboard/user-dashboard.component";
import {AdminDashboardComponent} from "./admin-dashboard/admin-dashboard.component";
import {AddFeedbackComponent} from "./add-feedback/add-feedback.component";
import {AddTrainingComponent} from "./add-training/add-training.component";
import {UserSignedUpSessionsComponent} from "./user-signed-up-sessions/user-signed-up-sessions.component";
import {UserInfoComponent} from "./user-info/user-info.component";
import {LogoutComponent} from "./logout/logout.component";

const routes: Routes = [
  { path: 'home', component: HomeComponent },
  { path: 'auth/login', component: LoginComponent },
  { path: 'signup', component: RegisterComponent },
  { path: 'user-dashboard', component: UserDashboardComponent, canActivate: [RoleGuard], data: { roles: ['ROLE_USER', 'ROLE_ADMIN'] }},
  { path: 'admin/dashboard', component: AdminDashboardComponent, canActivate: [authGuard], data: { roles: ['ROLE_ADMIN'] }},
  { path: 'user/training-sessions', component: TrainingSessionsComponent, canActivate: [RoleGuard], data: { roles: ['ROLE_USER', 'ROLE_ADMIN'] }},
  { path: 'admin/training-sessions', component: AddTrainingComponent, canActivate: [authGuard], data: { roles: ['ROLE_ADMIN'] }},
  { path: 'admin/add-feedback', component: AddFeedbackComponent, canActivate: [authGuard], data: { roles: ['ROLE_ADMIN'] }},
  { path: 'user/signed-up-sessions', component: UserSignedUpSessionsComponent, canActivate: [RoleGuard], data: { roles: ['ROLE_USER'] }},
  { path: 'user', component: UserComponent, canActivate: [RoleGuard], data: { roles: ['ROLE_USER', 'ROLE_ADMIN'] }},
  { path: 'admin', component: AdminComponent, canActivate: [authGuard], data: { roles: ['ROLE_ADMIN'] }},
  { path: 'user/info', component: UserInfoComponent, canActivate: [RoleGuard], data: { roles: ['ROLE_USER', 'ROLE_ADMIN'] }},
  { path: 'admin/logout', component: LogoutComponent, canActivate: [authGuard], data: { roles: ['ROLE_USER', 'ROLE_ADMIN'] }},
  { path: 'user/logout', component: LogoutComponent, canActivate: [authGuard], data: { roles: ['ROLE_USER'] }},
  { path: '', redirectTo: 'home', pathMatch: 'full' }
];

@NgModule({
  declarations: [
    AppComponent
  ],
  imports: [
    BrowserModule,
    HttpClientModule,
    FormsModule,
    ReactiveFormsModule,
    RouterModule.forRoot(routes),
    HomeComponent,
    RegisterComponent,
    LoginComponent,
    AdminComponent,
    UserComponent
  ],
  providers: [httpInterceptorProviders],
  bootstrap: [AppComponent]
})
export class AppModule { }
