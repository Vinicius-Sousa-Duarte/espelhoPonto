import { TestBed } from '@angular/core/testing';

import { Ponto } from './ponto';

describe('Ponto', () => {
  let service: Ponto;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(Ponto);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
