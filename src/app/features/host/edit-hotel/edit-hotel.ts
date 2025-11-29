import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { HotelService } from '../../../core/services/hotel.service';
import { AuthService } from '../../../core/services/auth.service';

interface HotelFormData {
  nombre: string;
  descripcion: string;
  telefono: string;
  emailContacto: string;
  estrellas: number;
  direccion: {
    calle: string;
    ciudad: string;
    estadoProvincia: string;
    pais: string;
    codigoPostal: string;
  };
  imagen?: string;
}

@Component({
  selector: 'app-edit-hotel',
  standalone: false,
  templateUrl: './edit-hotel.html',
  styleUrl: './edit-hotel.scss',
})
export class EditHotel implements OnInit {
  hotelId!: number;
  hotelData: HotelFormData = {
    nombre: '',
    descripcion: '',
    telefono: '',
    emailContacto: '',
    estrellas: 3,
    direccion: {
      calle: '',
      ciudad: '',
      estadoProvincia: '',
      pais: '',
      codigoPostal: ''
    }
  };

  loading = false;
  loadingData = true;
  errorMessage = '';
  successMessage = '';
  
  selectedImage: File | null = null;
  selectedImageName: string = '';
  imagePreview: string | null = null;
  currentImage: string | null = null;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private hotelService: HotelService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      this.hotelId = +params['id'];
      this.loadHotelData();
    });
  }

  loadHotelData(): void {
    this.loadingData = true;
    this.hotelService.getHotelById(this.hotelId).subscribe({
      next: (response) => {
        if (response.success && response.data) {
          const hotel = response.data;
          this.hotelData = {
            nombre: hotel.nombre,
            descripcion: hotel.descripcion,
            telefono: hotel.telefono,
            emailContacto: hotel.emailContacto,
            estrellas: hotel.estrellas,
            direccion: {
              calle: hotel.direccion.calle,
              ciudad: hotel.direccion.ciudad,
              estadoProvincia: hotel.direccion.estadoProvincia,
              pais: hotel.direccion.pais,
              codigoPostal: hotel.direccion.codigoPostal
            }
          };
          
          // Guardar y mostrar imagen actual si existe
          if (hotel.imagenPrincipal) {
            this.currentImage = hotel.imagenPrincipal;
            // Mostrar en el preview
            if (hotel.imagenPrincipal.startsWith('data:image')) {
              this.imagePreview = hotel.imagenPrincipal;
            } else {
              // Si es base64 sin prefijo, agregarlo
              this.imagePreview = `data:image/jpeg;base64,${hotel.imagenPrincipal}`;
            }
          } else if (hotel.imagenes && hotel.imagenes.length > 0) {
            // Si no hay imagenPrincipal, usar la primera de imagenes
            this.currentImage = hotel.imagenes[0];
            if (hotel.imagenes[0].startsWith('data:image')) {
              this.imagePreview = hotel.imagenes[0];
            } else {
              this.imagePreview = `data:image/jpeg;base64,${hotel.imagenes[0]}`;
            }
          }
        }
        this.loadingData = false;
      },
      error: (error) => {
        console.error('Error cargando hotel:', error);
        this.errorMessage = 'Error al cargar los datos del hotel';
        this.loadingData = false;
      }
    });
  }

  onSubmit(): void {
    if (this.loading) {
      return;
    }

    if (!this.validateForm()) {
      return;
    }

    this.loading = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.hotelService.updateHotel(this.hotelId, this.hotelData).subscribe({
      next: (response) => {
        if (response.success) {
          this.successMessage = 'Hotel actualizado exitosamente';
          setTimeout(() => {
            this.router.navigate(['/host']);
          }, 2000);
        } else {
          this.loading = false;
        }
      },
      error: (error) => {
        console.error('Error actualizando hotel:', error);
        this.errorMessage = error.error?.message || 'Error al actualizar el hotel';
        this.loading = false;
      }
    });
  }

  validateForm(): boolean {
    if (!this.hotelData.nombre || this.hotelData.nombre.trim().length < 3) {
      this.errorMessage = 'El nombre del hotel debe tener al menos 3 caracteres';
      return false;
    }

    if (!this.hotelData.descripcion || this.hotelData.descripcion.trim().length < 10) {
      this.errorMessage = 'La descripción debe tener al menos 10 caracteres';
      return false;
    }

    if (!this.hotelData.telefono) {
      this.errorMessage = 'El teléfono es obligatorio';
      return false;
    }

    if (!this.hotelData.emailContacto || !this.isValidEmail(this.hotelData.emailContacto)) {
      this.errorMessage = 'El email de contacto es inválido';
      return false;
    }

    if (!this.hotelData.direccion.calle || !this.hotelData.direccion.ciudad || 
        !this.hotelData.direccion.estadoProvincia || !this.hotelData.direccion.pais) {
      this.errorMessage = 'Todos los campos de dirección son obligatorios';
      return false;
    }

    return true;
  }

  isValidEmail(email: string): boolean {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
  }

  getStarsArray(): number[] {
    return [1, 2, 3, 4, 5];
  }

  onImageSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    
    if (input.files && input.files.length > 0) {
      const file = input.files[0];
      
      // Validar tipo de archivo
      const validTypes = ['image/jpeg', 'image/png', 'image/jpg', 'image/webp'];
      if (!validTypes.includes(file.type)) {
        this.errorMessage = 'Formato de imagen inválido. Use JPG, PNG o WEBP';
        return;
      }
      
      // Validar tamaño (5MB máximo)
      const maxSize = 5 * 1024 * 1024; // 5MB en bytes
      if (file.size > maxSize) {
        this.errorMessage = 'La imagen es muy grande. Máximo 5MB';
        return;
      }
      
      this.selectedImage = file;
      this.selectedImageName = file.name;
      
      // Crear vista previa
      const reader = new FileReader();
      reader.onload = (e: ProgressEvent<FileReader>) => {
        this.imagePreview = e.target?.result as string;
        this.hotelData.imagen = this.imagePreview;
      };
      reader.readAsDataURL(file);
      
      this.errorMessage = '';
    }
  }

  removeImage(): void {
    this.selectedImage = null;
    this.selectedImageName = '';
    
    // Restaurar imagen original si existe
    if (this.currentImage) {
      if (this.currentImage.startsWith('data:image')) {
        this.imagePreview = this.currentImage;
      } else {
        this.imagePreview = `data:image/jpeg;base64,${this.currentImage}`;
      }
      this.hotelData.imagen = undefined; // No enviar imagen si se mantiene la actual
    } else {
      this.imagePreview = null;
      this.hotelData.imagen = undefined;
    }
  }

  cancelEdit(): void {
    if (confirm('¿Estás seguro de cancelar? Los cambios no guardados se perderán.')) {
      this.router.navigate(['/host']);
    }
  }
}
