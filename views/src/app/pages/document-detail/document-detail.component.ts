import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';

import { DocumentApiService } from '../../core/document-api.service';
import { DocumentItem, DocumentStatus, DocumentVersion, UpdateDocumentPayload } from '../../models/document.model';

@Component({
  selector: 'app-document-detail',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './document-detail.component.html',
  styleUrl: './document-detail.component.css'
})
export class DocumentDetailComponent implements OnInit {
  documentId!: number;
  document?: DocumentItem;
  versions: DocumentVersion[] = [];

  loading = false;
  saving = false;
  uploading = false;
  error = '';

  readonly form = this.fb.nonNullable.group({
    title: ['', [Validators.required]],
    description: [''],
    ownerTenant: ['', [Validators.required]],
    tagsText: ['']
  });

  constructor(
    private readonly route: ActivatedRoute,
    private readonly fb: FormBuilder,
    private readonly api: DocumentApiService
  ) {}

  ngOnInit(): void {
    this.documentId = Number(this.route.snapshot.paramMap.get('id'));
    this.loadDocument();
    this.loadVersions();
  }

  loadDocument(): void {
    this.loading = true;
    this.api.getDocument(this.documentId).subscribe({
      next: (doc) => {
        this.document = doc;
        this.form.reset({
          title: doc.title,
          description: doc.description ?? '',
          ownerTenant: doc.ownerTenant,
          tagsText: doc.tags.join(', ')
        });
        this.loading = false;
      },
      error: () => {
        this.error = 'Não foi possível carregar o documento.';
        this.loading = false;
      }
    });
  }

  loadVersions(): void {
    this.api.listVersions(this.documentId).subscribe({
      next: (versions) => {
        this.versions = versions;
      },
      error: () => {
        this.error = 'Não foi possível carregar versões do documento.';
      }
    });
  }

  saveMetadata(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.saving = true;
    const raw = this.form.getRawValue();
    const payload: UpdateDocumentPayload = {
      title: raw.title,
      description: raw.description,
      ownerTenant: raw.ownerTenant,
      tags: raw.tagsText.split(',').map((t) => t.trim()).filter(Boolean)
    };

    this.api.updateMetadata(this.documentId, payload).subscribe({
      next: (doc) => {
        this.document = doc;
        this.saving = false;
      },
      error: () => {
        this.error = 'Falha ao salvar metadados.';
        this.saving = false;
      }
    });
  }

  publish(): void {
    this.changeStatus('PUBLISHED');
  }

  archive(): void {
    this.changeStatus('ARCHIVED');
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) {
      return;
    }

    this.uploading = true;
    this.api.uploadVersion(this.documentId, file).subscribe({
      next: () => {
        this.uploading = false;
        input.value = '';
        this.loadVersions();
      },
      error: () => {
        this.error = 'Falha no upload da nova versão.';
        this.uploading = false;
      }
    });
  }

  download(version: DocumentVersion): void {
    this.api.downloadVersion(this.documentId, version.versionNumber).subscribe({
      next: (blob) => {
        const url = URL.createObjectURL(blob);
        const anchor = document.createElement('a');
        anchor.href = url;
        anchor.download = version.originalFilename;
        anchor.click();
        URL.revokeObjectURL(url);
      },
      error: () => {
        this.error = 'Falha ao baixar versão.';
      }
    });
  }

  statusClass(status: DocumentStatus): string {
    return status.toLowerCase();
  }

  private changeStatus(status: DocumentStatus): void {
    const request = status === 'PUBLISHED'
      ? this.api.publish(this.documentId)
      : this.api.archive(this.documentId);

    request.subscribe({
      next: (doc) => {
        this.document = doc;
      },
      error: () => {
        this.error = 'Falha ao alterar status.';
      }
    });
  }
}
