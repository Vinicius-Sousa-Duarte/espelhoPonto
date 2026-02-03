import { ApplicationConfig, LOCALE_ID, importProvidersFrom } from '@angular/core'; 
import { provideRouter } from '@angular/router';
import { routes } from './app.routes';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';

import { registerLocaleData } from '@angular/common';
import localePt from '@angular/common/locales/pt';
import { MAT_DATE_LOCALE } from '@angular/material/core'; 

registerLocaleData(localePt); 

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes),
    provideAnimationsAsync(),
    provideHttpClient(withInterceptorsFromDi()),
    
    { provide: LOCALE_ID, useValue: 'pt-BR' }, 
    { provide: MAT_DATE_LOCALE, useValue: 'pt-BR' } 
  ]
};