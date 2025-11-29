import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';

export interface Room {
  id: number;
  numero: string;
  nombreCompleto?: string;
  nombreCorto?: string;
  descripcion?: string;
  precioBase: number;
  capacidad?: number;
  imagenPrincipal?: string;
  imagenes?: string[];
  disponible?: boolean;
}

@Component({
  selector: 'app-room-card',
  templateUrl: './room-card.component.html',
  styleUrls: ['./room-card.component.scss'],
  standalone: true,
  imports: [CommonModule]
})
export class RoomCardComponent {
  // @Input: Recibe datos del componente padre
  @Input() room!: Room;
  @Input() showPrice: boolean = true;
  @Input() showActions: boolean = true;
  @Input() currentImageIndex: number = 0;

  // @Output: Emite eventos al componente padre
  @Output() reserve = new EventEmitter<Room>();
  @Output() viewDetails = new EventEmitter<Room>();
  @Output() imageChange = new EventEmitter<{ room: Room, index: number }>();

  /**
   * Emite evento de reserva al padre
   */
  onReserveClick(): void {
    this.reserve.emit(this.room);
  }

  /**
   * Emite evento de ver detalles al padre
   */
  onViewDetailsClick(): void {
    this.viewDetails.emit(this.room);
  }

  /**
   * Navega a la siguiente imagen
   */
  nextImage(): void {
    if (this.room.imagenes && this.room.imagenes.length > 0) {
      const newIndex = (this.currentImageIndex + 1) % this.room.imagenes.length;
      this.imageChange.emit({ room: this.room, index: newIndex });
    }
  }

  /**
   * Navega a la imagen anterior
   */
  previousImage(): void {
    if (this.room.imagenes && this.room.imagenes.length > 0) {
      const newIndex = this.currentImageIndex === 0 
        ? this.room.imagenes.length - 1 
        : this.currentImageIndex - 1;
      this.imageChange.emit({ room: this.room, index: newIndex });
    }
  }

  /**
   * Obtiene la URL de la imagen actual
   */
  getCurrentImage(): string {
    if (this.room.imagenes && this.room.imagenes.length > 0) {
      return this.room.imagenes[this.currentImageIndex] || this.room.imagenPrincipal || '/assets/room-placeholder.jpg';
    }
    return this.room.imagenPrincipal || '/assets/room-placeholder.jpg';
  }

  /**
   * Verifica si hay múltiples imágenes
   */
  hasMultipleImages(): boolean {
    return (this.room.imagenes?.length || 0) > 1;
  }
}
