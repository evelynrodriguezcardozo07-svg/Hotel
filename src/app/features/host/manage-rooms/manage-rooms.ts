import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { RoomService, Room, RoomRequest } from '../../../core/services/room.service';
import { HotelService } from '../../../core/services/hotel.service';

@Component({
  selector: 'app-manage-rooms',
  standalone: false,
  templateUrl: './manage-rooms.html',
  styleUrl: './manage-rooms.scss',
})
export class ManageRooms implements OnInit {
  hotelId!: number;
  hotelName = '';
  rooms: Room[] = [];
  loading = false;
  
  // Estadísticas de disponibilidad
  availabilityStats = {
    total: 0,
    disponibles: 0,
    ocupadas: 0,
    mantenimiento: 0,
    inactivas: 0
  };
  
  showModal = false;
  editMode = false;
  roomData: RoomRequest = {
    hotelId: 0,
    numero: '',
    nombreCorto: '',
    roomTypeId: 1,
    precioBase: 0,
    capacidad: 1,
    numCamas: 1,
    metrosCuadrados: 0,
    estado: 'disponible',
    imagen: '',
    imagenes: []
  };
  selectedRoomId: number | null = null;
  
  submitting = false;
  message = '';
  error = '';

  // Variables para manejo de imágenes múltiples
  selectedImages: File[] = [];
  imagePreviews: { file: File, preview: string }[] = [];
  
  // Índice actual del carrusel para cada habitación
  currentImageIndex: { [key: number]: number } = {};

  // Tipos de habitación predefinidos (deberías cargarlos desde el backend)
  roomTypes = [
    { id: 1, nombre: 'Individual', descripcion: 'Habitación individual con una cama' },
    { id: 2, nombre: 'Doble', descripcion: 'Habitación doble con dos camas o una cama matrimonial' },
    { id: 3, nombre: 'Suite', descripcion: 'Suite de lujo con sala de estar' },
    { id: 4, nombre: 'Familiar', descripcion: 'Habitación espaciosa para familias' }
  ];

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private roomService: RoomService,
    private hotelService: HotelService
  ) {}

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      this.hotelId = +params['id'];
      if (this.hotelId) {
        this.loadHotelInfo();
        this.loadRooms();
      }
    });
  }

  loadHotelInfo(): void {
    this.hotelService.getHotelById(this.hotelId).subscribe({
      next: (response) => {
        if (response.success && response.data) {
          this.hotelName = response.data.nombre;
        }
      },
      error: (error) => console.error('Error cargando hotel:', error)
    });
  }

  loadRooms(): void {
    this.loading = true;
    this.roomService.getRoomsByHotel(this.hotelId).subscribe({
      next: (response) => {
        if (response.success && response.data) {
          this.rooms = response.data;
          this.calculateAvailabilityStats();
        }
        this.loading = false;
      },
      error: (error) => {
        console.error('Error cargando habitaciones:', error);
        this.loading = false;
      }
    });
  }

  calculateAvailabilityStats(): void {
    this.availabilityStats = {
      total: this.rooms.length,
      disponibles: this.rooms.filter(r => r.estado === 'disponible').length,
      ocupadas: this.rooms.filter(r => r.estado === 'ocupada').length,
      mantenimiento: this.rooms.filter(r => r.estado === 'mantenimiento').length,
      inactivas: this.rooms.filter(r => r.estado === 'inactivo').length
    };
  }

  openCreateModal(): void {
    this.editMode = false;
    this.roomData = {
      hotelId: this.hotelId,
      numero: '',
      nombreCorto: '',
      roomTypeId: 1,
      precioBase: 0,
      capacidad: 1,
      numCamas: 1,
      metrosCuadrados: 0,
      estado: 'disponible',
      imagen: '',
      imagenes: []
    };
    this.selectedImages = [];
    this.imagePreviews = [];
    this.showModal = true;
    this.message = '';
    this.error = '';
  }

  editRoom(room: Room): void {
    this.editMode = true;
    this.selectedRoomId = room.id || null;
    // Obtener el roomTypeId del objeto room (roomType o tipoHabitacion)
    const roomType = room.roomType || room.tipoHabitacion;
    this.roomData = {
      hotelId: this.hotelId,
      numero: room.numero,
      nombreCorto: room.nombreCorto || '',
      roomTypeId: roomType?.id || 0,
      precioBase: room.precioBase,
      capacidad: room.capacidad,
      numCamas: room.numCamas,
      metrosCuadrados: room.metrosCuadrados || 0,
      estado: room.estado
    };
    
    // Cargar imágenes existentes
    this.imagePreviews = [];
    if (room.imagenes && room.imagenes.length > 0) {
      room.imagenes.forEach((img, index) => {
        this.imagePreviews.push({
          file: new File([], `imagen-${index + 1}.jpg`),
          preview: `data:image/jpeg;base64,${img}`
        });
      });
    }
    
    this.showModal = true;
    this.message = '';
    this.error = '';
  }

  closeModal(): void {
    this.showModal = false;
    this.editMode = false;
    this.selectedRoomId = null;
  }

  submitRoom(): void {
    if (this.submitting) return;

    this.submitting = true;
    this.error = '';

    // Preparar las imágenes: primera es principal, resto son adicionales
    if (this.imagePreviews.length > 0) {
      // Primera imagen como principal (sin el prefijo data:image/...)
      this.roomData.imagen = this.imagePreviews[0].preview.split(',')[1];
      
      // Resto de imágenes como adicionales
      if (this.imagePreviews.length > 1) {
        this.roomData.imagenes = this.imagePreviews
          .slice(1)
          .map(img => img.preview.split(',')[1]);
      }
    }

    const action = this.editMode && this.selectedRoomId
      ? this.roomService.updateRoom(this.selectedRoomId, this.roomData)
      : this.roomService.createRoom(this.roomData);

    action.subscribe({
      next: (response) => {
        if (response.success) {
          this.message = this.editMode ? 'Habitación actualizada' : 'Habitación creada';
          setTimeout(() => {
            this.closeModal();
            this.loadRooms();
          }, 1500);
        }
        this.submitting = false;
      },
      error: (error) => {
        this.error = error.error?.message || 'Error al guardar habitación';
        this.submitting = false;
      }
    });
  }

  deleteRoom(room: Room): void {
    if (!confirm(`¿Eliminar habitación ${room.numero}?`)) return;

    this.roomService.deleteRoom(room.id).subscribe({
      next: (response) => {
        if (response.success) {
          this.loadRooms();
        }
      },
      error: (error) => {
        alert('Error al eliminar habitación');
        console.error(error);
      }
    });
  }

  changeRoomStatus(room: Room, newStatus: string): void {
    this.roomService.changeRoomStatus(room.id, newStatus).subscribe({
      next: (response) => {
        if (response.success) {
          this.loadRooms();
        }
      },
      error: (error) => {
        alert('Error al cambiar estado');
        console.error(error);
      }
    });
  }

  getStatusClass(estado: string): string {
    switch (estado.toLowerCase()) {
      case 'disponible':
        return 'bg-green-100 text-green-800';
      case 'mantenimiento':
        return 'bg-yellow-100 text-yellow-800';
      case 'inactivo':
        return 'bg-red-100 text-red-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  }

  getRoomType(room: Room): { nombre: string; descripcion: string } {
    // El backend puede devolver roomType o tipoHabitacion dependiendo del endpoint
    const type = room.roomType || room.tipoHabitacion;
    return type || { nombre: 'N/A', descripcion: '' };
  }

  goBack(): void {
    this.router.navigate(['/host']);
  }

  onImageSelected(event: any): void {
    const files: FileList = event.target.files;
    if (!files || files.length === 0) return;

    // Validar cada archivo
    const validTypes = ['image/jpeg', 'image/png', 'image/jpg', 'image/webp'];
    const maxSize = 5 * 1024 * 1024; // 5MB

    for (let i = 0; i < files.length; i++) {
      const file = files[i];

      // Validar tipo
      if (!validTypes.includes(file.type)) {
        this.error = `El archivo ${file.name} no es una imagen válida (JPG, PNG, WEBP)`;
        continue;
      }

      // Validar tamaño
      if (file.size > maxSize) {
        this.error = `La imagen ${file.name} supera los 5MB`;
        continue;
      }

      // Leer y convertir a base64
      const reader = new FileReader();
      reader.onload = (e: any) => {
        const base64 = e.target.result;
        
        // Agregar al array de previsualizaciones
        this.imagePreviews.push({
          file: file,
          preview: base64
        });
        
        this.error = '';
      };
      reader.readAsDataURL(file);
    }
  }

  removeImage(index: number): void {
    this.imagePreviews.splice(index, 1);
  }

  // Marcar imagen como principal
  setImageAsPrincipal(index: number): void {
    if (this.imagePreviews.length === 0) return;
    
    // Mover la imagen seleccionada al principio del array
    const [selected] = this.imagePreviews.splice(index, 1);
    this.imagePreviews.unshift(selected);
  }

  // Métodos para el carrusel de imágenes
  getCurrentImage(room: Room): string {
    if (!room.imagenes || room.imagenes.length === 0) return '';
    const index = this.currentImageIndex[room.id] || 0;
    return room.imagenes[index];
  }

  nextImage(room: Room, event: Event): void {
    event.stopPropagation();
    if (!room.imagenes || room.imagenes.length === 0) return;
    const currentIndex = this.currentImageIndex[room.id] || 0;
    this.currentImageIndex[room.id] = (currentIndex + 1) % room.imagenes.length;
  }

  previousImage(room: Room, event: Event): void {
    event.stopPropagation();
    if (!room.imagenes || room.imagenes.length === 0) return;
    const currentIndex = this.currentImageIndex[room.id] || 0;
    this.currentImageIndex[room.id] = currentIndex === 0 ? room.imagenes.length - 1 : currentIndex - 1;
  }

  getImageIndex(room: Room): number {
    return this.currentImageIndex[room.id] || 0;
  }
}
