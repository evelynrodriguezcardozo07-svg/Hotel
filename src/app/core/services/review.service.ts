import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiResponse } from '../../models/api-response.model';

export interface ReviewRequest {
  hotelId: number;
  puntuacion: number;
  comentario: string;
  reservaId?: number;
  puntuacionLimpieza?: number;
  puntuacionServicio?: number;
  puntuacionUbicacion?: number;
}

export interface ReviewResponse {
  id: number;
  usuario: {
    id: number;
    nombre: string;
  };
  puntuacion: number;
  comentario: string;
  respuesta?: string;
  creadoEn: string;
  verificado?: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class ReviewService {
  private apiUrl = 'http://localhost:8080/api/reviews';

  constructor(private http: HttpClient) {}

  crearReview(request: ReviewRequest): Observable<ApiResponse<ReviewResponse>> {
    return this.http.post<ApiResponse<ReviewResponse>>(this.apiUrl, request);
  }

  getReviewsByHotel(hotelId: number, page: number = 0, size: number = 10): Observable<ApiResponse<any>> {
    return this.http.get<ApiResponse<any>>(`${this.apiUrl}/hotel/${hotelId}?page=${page}&size=${size}`);
  }

  getReviewsVerificadas(hotelId: number): Observable<ApiResponse<ReviewResponse[]>> {
    return this.http.get<ApiResponse<ReviewResponse[]>>(`${this.apiUrl}/hotel/${hotelId}/verificadas`);
  }

  getMisReviews(): Observable<ApiResponse<ReviewResponse[]>> {
    return this.http.get<ApiResponse<ReviewResponse[]>>(`${this.apiUrl}/mis-reviews`);
  }

  responderReview(id: number, respuesta: string): Observable<ApiResponse<ReviewResponse>> {
    return this.http.post<ApiResponse<ReviewResponse>>(`${this.apiUrl}/${id}/responder`, null, {
      params: { respuesta }
    });
  }

  marcarUtil(id: number): Observable<ApiResponse<ReviewResponse>> {
    return this.http.post<ApiResponse<ReviewResponse>>(`${this.apiUrl}/${id}/util`, null);
  }

  eliminarReview(id: number): Observable<ApiResponse<string>> {
    return this.http.delete<ApiResponse<string>>(`${this.apiUrl}/${id}`);
  }
}
