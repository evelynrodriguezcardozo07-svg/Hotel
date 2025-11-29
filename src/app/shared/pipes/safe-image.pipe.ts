import { Pipe, PipeTransform } from '@angular/core';
import { DomSanitizer, SafeUrl } from '@angular/platform-browser';

@Pipe({
  name: 'safeImage',
  standalone: false
})
export class SafeImagePipe implements PipeTransform {
  constructor(private sanitizer: DomSanitizer) {}

  transform(value: string | null | undefined): SafeUrl | string {
    if (!value) {
      return '';
    }

    // Si ya tiene el prefijo data:image, devolverlo tal cual
    if (value.startsWith('data:image')) {
      return this.sanitizer.bypassSecurityTrustUrl(value);
    }

    // Si es una URL completa (http/https), devolverla tal cual
    if (value.startsWith('http://') || value.startsWith('https://')) {
      return value;
    }

    // Si es base64 sin prefijo, agregarlo
    // Detectar el tipo de imagen por los primeros caracteres
    let prefix = 'data:image/jpeg;base64,';
    
    if (value.startsWith('/9j/')) {
      prefix = 'data:image/jpeg;base64,';
    } else if (value.startsWith('iVBOR')) {
      prefix = 'data:image/png;base64,';
    } else if (value.startsWith('R0lGOD')) {
      prefix = 'data:image/gif;base64,';
    } else if (value.startsWith('UklGR')) {
      prefix = 'data:image/webp;base64,';
    }

    return this.sanitizer.bypassSecurityTrustUrl(prefix + value);
  }
}
