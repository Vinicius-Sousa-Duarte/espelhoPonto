import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { Router, RouterModule } from '@angular/router';
import { UserService } from '../../services/user';

@Component({
  selector: 'app-cadastro-usuario',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
    MatSnackBarModule,
    RouterModule
  ],
  templateUrl: './cadastro-usuario.html',
  styleUrls: ['./cadastro-usuario.scss']
})
export class CadastroUsuarioComponent {
  private fb = inject(FormBuilder);
  private userService = inject(UserService);
  private snackBar = inject(MatSnackBar);
  private router = inject(Router);

  form = this.fb.group({
    nome: ['', Validators.required],
    login: ['', [Validators.required, Validators.email]],
    senha: ['', [Validators.required, Validators.minLength(6)]],
    regra: ['USER', Validators.required] 
  });

  onSubmit() {
    if (this.form.invalid) return;

    this.userService.cadastrar(this.form.value).subscribe({
      next: () => {
        this.snackBar.open('Usuário cadastrado com sucesso! 🚀', 'OK', {
          duration: 4000,
          panelClass: ['success-snackbar']
        });
        this.form.reset({ regra: 'USER' });
      },
      error: (err) => {
        const msg = err.error?.mensagem || 'Erro ao cadastrar usuário.';
        this.snackBar.open(msg, 'Fechar', {
          duration: 5000,
          panelClass: ['error-snackbar']
        });
      }
    });
  }

  voltar() {
    this.router.navigate(['/dashboard']);
  }
}