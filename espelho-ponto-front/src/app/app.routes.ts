import { Routes, Router } from '@angular/router';
import { inject } from '@angular/core';

import { LoginComponent } from './pages/login/login';
import { MainLayoutComponent } from './layouts/main-layout/main-layout';
import { DashboardComponent } from './pages/dashboard/dashboard';

import { AuthService } from './services/auth';

const authGuard = () => {
  const router = inject(Router);
  if (localStorage.getItem('auth-token')) {
    return true;
  }
  return router.parseUrl('/login');
};

const adminGuard = () => {
  const router = inject(Router);
  const authService = inject(AuthService);

  if (authService.hasRole('ADMIN')) {
    return true;
  }

  return router.parseUrl('/dashboard');
};

export const routes: Routes = [
  {
    path: 'login',
    component: LoginComponent
  },

  {
    path: '',
    component: MainLayoutComponent,
    canActivate: [authGuard],
    children: [

      {
        path: 'dashboard',
        component: DashboardComponent
      },

      {
        path: 'admin/usuarios',
        loadComponent: () => import('./pages/cadastro-usuario/cadastro-usuario').then(m => m.CadastroUsuarioComponent),
        canActivate: [adminGuard]
      },

      {
        path: '',
        redirectTo: 'dashboard',
        pathMatch: 'full'
      }
    ]
  },

  { path: '**', redirectTo: 'login' }
];