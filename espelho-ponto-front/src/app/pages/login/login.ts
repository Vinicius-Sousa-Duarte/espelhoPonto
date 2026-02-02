import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { AuthService } from '../../services/auth';
import { Router } from '@angular/router';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatSnackBarModule,
  ],
  templateUrl: './login.html',
  styleUrls: ['./login.scss'],
})
export class LoginComponent {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);
  private snackBar = inject(MatSnackBar);

  hidePassword = true;

  form = this.fb.group({
    login: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(3)]],
  });

  onSubmit() {
    if (this.form.invalid) return;

    const credentials = this.form.getRawValue();

    this.authService.login(credentials as any).subscribe({
      next: () => {
        this.snackBar.open('Login realizado com sucesso!', 'OK', { duration: 3000 });

        this.router.navigate(['/dashboard']);
      },
      error: (err) => {
        this.snackBar.open('Erro ao logar. Verifique login e senha.', 'Fechar', {
          duration: 5000,
          panelClass: ['error-snackbar'],
        });
        console.error(err);
      },
    });
  }
}
