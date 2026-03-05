import { Component, inject, OnInit } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { CommonModule } from '@angular/common'; 
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { AuthService } from '../../services/auth';

@Component({
  selector: 'app-main-layout',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive, MatSidenavModule, MatListModule, MatIconModule],
  templateUrl: './main-layout.html',
  styleUrls: ['./main-layout.scss']
})
export class MainLayoutComponent implements OnInit {
  private authService = inject(AuthService);
  isAdmin = false;

  menuAberto = true; 

  toggleMenu() {
    this.menuAberto = !this.menuAberto;
  }


  ngOnInit() {
    this.isAdmin = this.authService.hasRole('ADMIN');
  }

  logout() {
    this.authService.logout();
    window.location.reload();
  }
}