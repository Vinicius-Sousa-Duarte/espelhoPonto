import { Routes } from '@angular/router';
import { LoginComponent } from './pages/login/login';
import { MainLayoutComponent } from './layouts/main-layout/main-layout';
import { DashboardComponent } from './pages/dashboard/dashboard';
import { HistoryComponent } from './pages/history/history';
import { inject } from '@angular/core';
import { Router } from '@angular/router';

const authGuard = () => {
  const router = inject(Router);
  if (localStorage.getItem('auth-token')) {
    return true;
  }
  return router.parseUrl('/login');
};

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { 
    path: '', 
    component: MainLayoutComponent,
    canActivate: [authGuard], 
    children: [
      { path: 'dashboard', component: DashboardComponent },
      { path: 'historico', component: HistoryComponent },
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' }
    ]
  }
];