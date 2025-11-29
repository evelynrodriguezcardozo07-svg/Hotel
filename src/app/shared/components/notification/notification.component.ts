import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { trigger, state, style, transition, animate } from '@angular/animations';
import { NotificationService, Notification } from '../../../core/services/notification.service';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-notification',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="fixed top-4 right-4 z-50 space-y-3 w-96">
      <div *ngFor="let notification of notifications" 
           [@slideIn]
           class="shadow-2xl border-l-4 p-4 flex items-start gap-3 backdrop-blur-sm"
           [ngClass]="{
             'bg-green-50 border-green-500': notification.type === 'success',
             'bg-red-50 border-red-500': notification.type === 'error',
             'bg-yellow-50 border-yellow-500': notification.type === 'warning',
             'bg-blue-50 border-blue-500': notification.type === 'info'
           }">
        
        <!-- Icono -->
        <div class="flex-shrink-0">
          <!-- Success Icon -->
          <svg *ngIf="notification.type === 'success'" class="h-6 w-6 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"></path>
          </svg>
          
          <!-- Error Icon -->
          <svg *ngIf="notification.type === 'error'" class="h-6 w-6 text-red-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 14l2-2m0 0l2-2m-2 2l-2-2m2 2l2 2m7-2a9 9 0 11-18 0 9 9 0 0118 0z"></path>
          </svg>
          
          <!-- Warning Icon -->
          <svg *ngIf="notification.type === 'warning'" class="h-6 w-6 text-yellow-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"></path>
          </svg>
          
          <!-- Info Icon -->
          <svg *ngIf="notification.type === 'info'" class="h-6 w-6 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path>
          </svg>
        </div>

        <!-- Mensaje -->
        <div class="flex-1 min-w-0">
          <p class="text-sm font-medium"
             [ngClass]="{
               'text-green-900': notification.type === 'success',
               'text-red-900': notification.type === 'error',
               'text-yellow-900': notification.type === 'warning',
               'text-blue-900': notification.type === 'info'
             }">
            {{ notification.message }}
          </p>
        </div>

        <!-- Botón cerrar -->
        <button (click)="remove(notification.id)" 
                class="flex-shrink-0 ml-2 text-gray-400 hover:text-gray-600 transition-colors">
          <svg class="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
          </svg>
        </button>
      </div>
    </div>
  `,
  animations: [
    trigger('slideIn', [
      transition(':enter', [
        style({ transform: 'translateX(100%)', opacity: 0 }),
        animate('300ms ease-out', style({ transform: 'translateX(0)', opacity: 1 }))
      ]),
      transition(':leave', [
        animate('200ms ease-in', style({ transform: 'translateX(100%)', opacity: 0 }))
      ])
    ])
  ]
})
export class NotificationComponent implements OnInit, OnDestroy {
  notifications: Notification[] = [];
  private subscription?: Subscription;
  private timers: Map<string, any> = new Map();

  constructor(private notificationService: NotificationService) {}

  ngOnInit(): void {
    this.subscription = this.notificationService.notifications$.subscribe(
      (notification) => {
        this.notifications.push(notification);
        
        // Auto-remover después de la duración especificada
        if (notification.duration) {
          const timer = setTimeout(() => {
            this.remove(notification.id);
          }, notification.duration);
          this.timers.set(notification.id, timer);
        }
      }
    );
  }

  ngOnDestroy(): void {
    this.subscription?.unsubscribe();
    
    // Limpiar todos los timers
    this.timers.forEach(timer => clearTimeout(timer));
    this.timers.clear();
  }

  remove(id: string): void {
    this.notifications = this.notifications.filter(n => n.id !== id);
    
    // Limpiar el timer si existe
    const timer = this.timers.get(id);
    if (timer) {
      clearTimeout(timer);
      this.timers.delete(id);
    }
  }
}
