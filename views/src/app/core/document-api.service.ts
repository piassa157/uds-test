import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import {
  CreateDocumentPayload,
  DocumentItem,
  DocumentStatus,
  DocumentVersion,
  FileUploadResponse,
  PagedResult,
  UpdateDocumentPayload
} from '../models/document.model';

@Injectable({ providedIn: 'root' })
export class DocumentApiService {
  constructor(private readonly http: HttpClient) {}

  listDocuments(filters: {
    page: number;
    size: number;
    title?: string;
    status?: DocumentStatus | '';
    sort?: string;
  }): Observable<PagedResult<DocumentItem>> {
    let params = new HttpParams()
      .set('page', String(filters.page))
      .set('size', String(filters.size))
      .set('sort', filters.sort ?? 'createdAt,desc');

    if (filters.title?.trim()) {
      params = params.set('title', filters.title.trim());
    }

    if (filters.status) {
      params = params.set('status', filters.status);
    }

    return this.http.get<PagedResult<DocumentItem>>('/api/documents', { params });
  }

  getDocument(id: number): Observable<DocumentItem> {
    return this.http.get<DocumentItem>(`/api/documents/${id}`);
  }

  createDocument(payload: CreateDocumentPayload): Observable<DocumentItem> {
    return this.http.post<DocumentItem>('/api/documents', payload);
  }

  updateMetadata(id: number, payload: UpdateDocumentPayload): Observable<DocumentItem> {
    return this.http.put<DocumentItem>(`/api/documents/${id}`, payload);
  }

  publish(id: number): Observable<DocumentItem> {
    return this.http.patch<DocumentItem>(`/api/documents/${id}/publish`, {});
  }

  archive(id: number): Observable<DocumentItem> {
    return this.http.patch<DocumentItem>(`/api/documents/${id}/archive`, {});
  }

  uploadVersion(id: number, file: File): Observable<FileUploadResponse> {
    const body = new FormData();
    body.append('file', file);
    return this.http.post<FileUploadResponse>(`/api/documents/${id}/files`, body);
  }

  listVersions(id: number): Observable<DocumentVersion[]> {
    return this.http.get<DocumentVersion[]>(`/api/documents/${id}/files`);
  }

  downloadVersion(id: number, versionNumber: number): Observable<Blob> {
    return this.http.get(`/api/documents/${id}/files/${versionNumber}`, {
      responseType: 'blob'
    });
  }
}
