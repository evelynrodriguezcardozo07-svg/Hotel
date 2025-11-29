import { Injectable } from '@angular/core';
import { HttpInterceptor, HttpRequest, HttpHandler, HttpEvent, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, retry } from 'rxjs/operators';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { NotificationService } from '../services/notification.service';

@Injectable()
export class ErrorInterceptor implements HttpInterceptor {
  
  constructor(
    private authService: AuthService,
    private router: Router,
    private notificationService: NotificationService
  ) {}

  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    return next.handle(request).pipe(
      retry(0), // No reintentar automáticamente
      catchError((error: HttpErrorResponse) => {
        let errorMessage = 'Ocurrió un error';

        if (error.error instanceof ErrorEvent) {
          // Error del lado del cliente
          console.error('Error del cliente:', error.error.message);
          errorMessage = 'Error de conexión. Verifica tu internet.';
        } else {
          // Error del lado del servidor
          console.error(`Error ${error.status}: ${error.message}`);
          
          switch (error.status) {
            case 400:
              // Intentar extraer mensaje del servidor
              if (error.error?.message) {
                errorMessage = error.error.message;
              } else if (error.error?.error) {
                errorMessage = error.error.error;
              } else if (typeof error.error === 'string') {
                errorMessage = error.error;
              } else {
                // Mensajes específicos según el endpoint
                if (request.url.includes('/reservas')) {
                  errorMessage = 'No se pudo completar la reserva. Por favor verifica que has iniciado sesión y que la habitación esté disponible para las fechas seleccionadas.';
                } else if (request.url.includes('/auth/login')) {
                  errorMessage = 'Credenciales incorrectas. Verifica tu correo y contraseña.';
                } else if (request.url.includes('/auth/register')) {
                  errorMessage = 'No se pudo completar el registro. Verifica que todos los campos sean correctos.';
                } else if (request.url.includes('/hotels')) {
                  errorMessage = 'No se pudo procesar la información del hotel. Verifica los datos ingresados.';
                } else if (request.url.includes('/habitaciones')) {
                  errorMessage = 'No se pudo procesar la información de la habitación. Verifica los datos ingresados.';
                } else {
                  errorMessage = 'Los datos enviados no son válidos. Por favor verifica la información.';
                }
              }
              break;
            case 401:
              errorMessage = 'No autorizado. Por favor inicia sesión.';
              this.authService.logout();
              this.router.navigate(['/auth/login']);
              break;
            case 403:
              errorMessage = 'No tienes permisos para realizar esta acción';
              this.router.navigate(['/']);
              break;
            case 404:
              errorMessage = 'Recurso no encontrado';
              break;
            case 409:
              errorMessage = error.error?.message || 'Conflicto con el recurso';
              break;
            case 422:
              errorMessage = error.error?.message || 'Datos inválidos';
              break;
            case 429:
              errorMessage = 'Demasiadas peticiones. Intenta más tarde.';
              break;
            case 500:
              errorMessage = 'Error del servidor. Intenta más tarde.';
              break;
            case 503:
              errorMessage = 'Servicio no disponible. Intenta más tarde.';
              break;
            default:
              errorMessage = `Error ${error.status}: ${error.statusText}`;
          }
        }

        // Mostrar notificación al usuario
        this.notificationService.error(errorMessage);

        return throwError(() => ({
          status: error.status,
          message: errorMessage,
          error: error.error
        }));
      })
    );
  }
}
