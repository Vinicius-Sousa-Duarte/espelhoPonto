import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CadastroUsuario } from './cadastro-usuario';

describe('CadastroUsuario', () => {
  let component: CadastroUsuario;
  let fixture: ComponentFixture<CadastroUsuario>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CadastroUsuario]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CadastroUsuario);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
