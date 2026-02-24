import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { UserService } from '../../services/user'; 
import { MatSnackBar } from '@angular/material/snack-bar';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-cadastro-usuario',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, MatCardModule, MatInputModule, MatButtonModule, MatSelectModule, MatIconModule],
  templateUrl: './cadastro-usuario.html',
  styleUrls: ['./cadastro-usuario.scss']
})
export class CadastroUsuarioComponent {
  private fb = inject(FormBuilder);
  private userService = inject(UserService);
  private snackBar = inject(MatSnackBar);

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
        this.snackBar.open('Sucesso!', 'OK', { duration: 3000 });
        this.form.reset({ regra: 'USER' });
      },
      error: (err) => {
        this.snackBar.open('Erro: ' + (err.error || 'Falha ao criar'), 'Fechar', { duration: 3000 });
      }
    });
  }
}