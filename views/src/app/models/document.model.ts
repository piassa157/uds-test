export type DocumentStatus = 'DRAFT' | 'PUBLISHED' | 'ARCHIVED';

export interface DocumentItem {
  id: number;
  title: string;
  description: string;
  tags: string[];
  ownerTenant: string;
  status: DocumentStatus;
  createdAt: string;
  updatedAt: string;
}

export interface DocumentVersion {
  id: number;
  versionNumber: number;
  fileKey: string;
  originalFilename: string;
  contentType: string;
  sizeBytes: number;
  uploadedAt: string;
  uploadedBy: string;
}

export interface FileUploadResponse {
  documentId: number;
  versionNumber: number;
  fileKey: string;
  uploadedAt: string;
  uploadedBy: string;
}

export interface CreateDocumentPayload {
  title: string;
  description: string;
  tags: string[];
  ownerTenant: string;
  status?: DocumentStatus;
}

export interface UpdateDocumentPayload {
  title: string;
  description: string;
  tags: string[];
  ownerTenant: string;
}

export interface PagedResult<T> {
  content: T[];
  number: number;
  size: number;
  totalElements: number;
  totalPages: number;
}
