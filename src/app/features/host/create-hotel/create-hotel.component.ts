import { Component } from '@angular/core';
import { Router } from '@angular/router';
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
  destacado: boolean;
  imagen?: string;
}

@Component({
  selector: 'app-create-hotel',
  templateUrl: './create-hotel.component.html',
  styleUrls: ['./create-hotel.component.scss'],
  standalone: false
})
export class CreateHotelComponent {
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
    },
    destacado: false
  };

  loading = false;
  errorMessage = '';
  successMessage = '';
  
  selectedImage: File | null = null;
  selectedImageName: string = '';
  imagePreview: string | null = null;

  constructor(
    private hotelService: HotelService,
    private authService: AuthService,
    public router: Router
  ) {
    // Pre-llenar email con el del usuario logueado
    const currentUser = this.authService.getCurrentUser();
    if (currentUser) {
      this.hotelData.emailContacto = currentUser.email;
    }
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
    this.imagePreview = null;
    this.hotelData.imagen = undefined;
  }

  onSubmit(): void {
    // Prevenir múltiples envíos
    if (this.loading) {
      return;
    }

    if (!this.validateForm()) {
      return;
    }

    this.loading = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.hotelService.createHotel(this.hotelData).subscribe({
      next: (response) => {
        if (response.success) {
          this.successMessage = '¡Hotel creado exitosamente! Será revisado por un administrador.';
          // Limpiar el formulario para prevenir re-envíos accidentales
          this.hotelData = {
            nombre: '',
            descripcion: '',
            telefono: '',
            emailContacto: this.authService.getCurrentUser()?.email || '',
            estrellas: 3,
            direccion: {
              calle: '',
              ciudad: '',
              estadoProvincia: '',
              pais: '',
              codigoPostal: ''
            },
            destacado: false
          };
          
          // Limpiar imagen
          this.removeImage();
          
          setTimeout(() => {
            this.router.navigate(['/host']);
          }, 2000);
        } else {
          this.loading = false;
        }
      },
      error: (error) => {
        console.error('Error creando hotel:', error);
        this.errorMessage = error.error?.message || 'Error al crear el hotel';
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
}
