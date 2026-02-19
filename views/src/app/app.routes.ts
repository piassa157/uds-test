import { Routes } from '@angular/router';

import { authGuard } from './core/auth.guard';
import { LoginComponent } from './pages/login/login.component';
import { DocumentsPageComponent } from './pages/documents/documents-page.component';
import { DocumentDetailComponent } from './pages/document-detail/document-detail.component';

export const routes: Routes = [
  {
    path: 'login',
    component: LoginComponent
  },
  {
    path: 'documents',
    canActivate: [authGuard],
    component: DocumentsPageComponent
  },
  {
    path: 'documents/:id',
    canActivate: [authGuard],
    component: DocumentDetailComponent
  },
  {
    path: '',
    pathMatch: 'full',
    redirectTo: 'documents'
  },
  {
    path: '**',
    redirectTo: 'documents'
  }
];
