import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import { DocumentApiService } from '../../core/document-api.service';
import { CreateDocumentPayload, DocumentItem, DocumentStatus, PagedResult } from '../../models/document.model';

@Component({
  selector: 'app-documents-page',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './documents-page.component.html',
  styleUrl: './documents-page.component.css'
})
export class DocumentsPageComponent implements OnInit {
  readonly statuses: Array<DocumentStatus | ''> = ['', 'DRAFT', 'PUBLISHED', 'ARCHIVED'];

  documents: DocumentItem[] = [];
  pageResult?: PagedResult<DocumentItem>;

  page = 0;
  size = 8;

  loading = false;
  creating = false;
  error = '';

  readonly filterForm = this.fb.nonNullable.group({
    title: [''],
    status: ['' as DocumentStatus | '']
  });

  readonly createForm = this.fb.nonNullable.group({
    title: ['', [Validators.required]],
    description: [''],
    tagsText: [''],
    ownerTenant: ['', [Validators.required]],
    status: ['DRAFT' as DocumentStatus]
  });

  constructor(
    private readonly fb: FormBuilder,
    private readonly api: DocumentApiService,
    private readonly router: Router
  ) {}

  ngOnInit(): void {
    this.loadDocuments();
  }

  loadDocuments(): void {
    this.loading = true;
    this.error = '';

    const { title, status } = this.filterForm.getRawValue();

    this.api.listDocuments({
      title,
      status,
      page: this.page,
      size: this.size,
      sort: 'createdAt,desc'
    }).subscribe({
      next: (result) => {
        this.pageResult = result;
        this.documents = result.content;
        this.loading = false;
      },
      error: () => {
        this.error = 'Falha ao carregar documentos.';
        this.loading = false;
      }
    });
  }

  applyFilters(): void {
    this.page = 0;
    this.loadDocuments();
  }

  resetFilters(): void {
    this.filterForm.reset({ title: '', status: '' });
    this.applyFilters();
  }

  changePage(next: number): void {
    if (!this.pageResult) {
      return;
    }

    if (next < 0 || next >= this.pageResult.totalPages) {
      return;
    }

    this.page = next;
    this.loadDocuments();
  }

  createDocument(): void {
    if (this.createForm.invalid) {
      this.createForm.markAllAsTouched();
      return;
    }

    const raw = this.createForm.getRawValue();
    const payload: CreateDocumentPayload = {
      title: raw.title,
      description: raw.description,
      ownerTenant: raw.ownerTenant,
      status: raw.status,
      tags: this.parseTags(raw.tagsText)
    };

    this.creating = true;
    this.api.createDocument(payload).subscribe({
      next: (document) => {
        this.creating = false;
        this.createForm.reset({
          title: '',
          description: '',
          tagsText: '',
          ownerTenant: '',
          status: 'DRAFT'
        });
        this.router.navigate(['/documents', document.id]);
      },
      error: () => {
        this.error = 'Não foi possível criar o documento.';
        this.creating = false;
      }
    });
  }

  statusClass(status: DocumentStatus): string {
    return status.toLowerCase();
  }

  private parseTags(tagsText: string): string[] {
    return tagsText
      .split(',')
      .map((tag) => tag.trim())
      .filter((tag) => tag.length > 0);
  }
}
