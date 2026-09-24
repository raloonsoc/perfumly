export interface PerfumeSummary {
  id: string;
  name: string;
  brandName: string;
  gender: 'MALE' | 'FEMALE' | 'UNISEX';
  year: number | null;
  mainAccords: string[];
};

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number; // actual page
  size: number;
}


export interface BrandSummary {
  id: string;
  name: string;
  country: string;
};
