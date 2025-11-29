package com.example.PROYECTO.FINAL_WEB.service;

import com.example.PROYECTO.FINAL_WEB.dto.request.HabitacionRequest;
import com.example.PROYECTO.FINAL_WEB.dto.response.HabitacionResponse;
import com.example.PROYECTO.FINAL_WEB.entity.*;
import com.example.PROYECTO.FINAL_WEB.exception.BusinessException;
import com.example.PROYECTO.FINAL_WEB.exception.ResourceNotFoundException;
import com.example.PROYECTO.FINAL_WEB.repository.*;
import com.example.PROYECTO.FINAL_WEB.util.AppConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio de gestión de habitaciones
 */
@Service
public class HabitacionService {

    @Autowired
    private HabitacionRepository habitacionRepository;

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private TipoHabitacionRepository tipoHabitacionRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private HotelService hotelService;

    @Autowired
    private HabitacionImagenRepository habitacionImagenRepository;

    /**
     * Crear nueva habitación (solo propietario del hotel)
     */
    @Transactional
    public HabitacionResponse crearHabitacion(HabitacionRequest request, Long propietarioId) {
        // Verificar que el hotel existe
        Hotel hotel = hotelRepository.findById(request.getHotelId())
                .orElseThrow(() -> new ResourceNotFoundException("Hotel", "id", request.getHotelId()));

        // Verificar que el usuario es el propietario del hotel
        if (!hotel.getPropietario().getId().equals(propietarioId)) {
            throw new BusinessException("No tiene permisos para agregar habitaciones a este hotel");
        }

        // Verificar que el tipo de habitación existe
        TipoHabitacion tipoHabitacion = tipoHabitacionRepository.findById(request.getRoomTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("TipoHabitacion", "id", request.getRoomTypeId()));

        // Verificar que el número de habitación no esté duplicado en el hotel
        if (habitacionRepository.existsByHotelIdAndNumero(request.getHotelId(), request.getNumero(), null)) {
            throw new BusinessException("Ya existe una habitación con el número " + request.getNumero() + " en este hotel");
        }

        // Crear habitación
        Habitacion habitacion = Habitacion.builder()
                .hotel(hotel)
                .roomType(tipoHabitacion)
                .numero(request.getNumero())
                .nombreCorto(request.getNombreCorto())
                .precioBase(request.getPrecioBase())
                .capacidad(request.getCapacidad())
                .numCamas(request.getNumCamas())
                .metrosCuadrados(request.getMetrosCuadrados())
                .estado(AppConstants.HABITACION_DISPONIBLE)
                .build();

        habitacion = habitacionRepository.save(habitacion);

        // Procesar imágenes
        int orden = 0;
        
        // Si hay imagen principal, la guardamos primero
        if (request.getImagen() != null && !request.getImagen().trim().isEmpty()) {
            HabitacionImagen imagenPrincipal = HabitacionImagen.builder()
                    .habitacion(habitacion)
                    .url(request.getImagen())
                    .altText(habitacion.getNumero() + " - " + habitacion.getNombreCorto())
                    .orden(orden++)
                    .esPrincipal(true)
                    .build();
            habitacionImagenRepository.save(imagenPrincipal);
        }
        
        // Procesar el resto de imágenes si existen
        if (request.getImagenes() != null && !request.getImagenes().isEmpty()) {
            for (String imagenBase64 : request.getImagenes()) {
                if (imagenBase64 != null && !imagenBase64.trim().isEmpty()) {
                    HabitacionImagen imagen = HabitacionImagen.builder()
                            .habitacion(habitacion)
                            .url(imagenBase64)
                            .altText(habitacion.getNumero() + " - Imagen adicional")
                            .orden(orden++)
                            // Solo es principal si no hay imagen principal definida y es la primera
                            .esPrincipal(request.getImagen() == null && orden == 1)
                            .build();
                    habitacionImagenRepository.save(imagen);
                }
            }
        }

        // Forzar el flush para asegurar que la habitación esté persistida antes de actualizar precios
        habitacionRepository.flush();
        
        // Actualizar rango de precios del hotel
        hotelService.actualizarRangoPreciosHotel(hotel.getId());

        return mapToResponse(habitacion);
    }

    /**
     * Obtener habitación por ID con detalles
     */
    @Transactional(readOnly = true)
    public HabitacionResponse getHabitacionById(Long id) {
        Habitacion habitacion = habitacionRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Habitacion", "id", id));

        return mapToResponse(habitacion);
    }

    /**
     * Obtener habitaciones de un hotel
     */
    @Transactional(readOnly = true)
    public List<HabitacionResponse> getHabitacionesByHotelId(Long hotelId) {
        return habitacionRepository.findByHotelId(hotelId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtener habitaciones disponibles de un hotel
     */
    @Transactional(readOnly = true)
    public List<HabitacionResponse> getHabitacionesDisponibles(Long hotelId) {
        return habitacionRepository.findDisponiblesByHotelId(hotelId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Buscar habitaciones disponibles con filtros
     */
    @Transactional(readOnly = true)
    public List<HabitacionResponse> buscarHabitacionesDisponibles(
            Long hotelId,
            LocalDate fechaCheckin,
            LocalDate fechaCheckout,
            Integer cantidadHuespedes) {

        return habitacionRepository.findDisponiblesConFiltros(
                hotelId, fechaCheckin, fechaCheckout, cantidadHuespedes
        ).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Verificar disponibilidad de una habitación
     */
    @Transactional(readOnly = true)
    public boolean verificarDisponibilidad(Long habitacionId, LocalDate fechaCheckin, LocalDate fechaCheckout) {
        return habitacionRepository.isHabitacionDisponible(habitacionId, fechaCheckin, fechaCheckout);
    }

    /**
     * Actualizar habitación
     */
    @Transactional
    public HabitacionResponse actualizarHabitacion(Long id, HabitacionRequest request, Long propietarioId) {
        Habitacion habitacion = habitacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Habitacion", "id", id));

        // Verificar que el usuario es el propietario del hotel
        if (!habitacion.getHotel().getPropietario().getId().equals(propietarioId)) {
            throw new BusinessException("No tiene permisos para actualizar esta habitación");
        }

        // Verificar número de habitación único (excluyendo la actual)
        if (!habitacion.getNumero().equals(request.getNumero())) {
            if (habitacionRepository.existsByHotelIdAndNumero(habitacion.getHotel().getId(), request.getNumero(), id)) {
                throw new BusinessException("Ya existe una habitación con el número " + request.getNumero());
            }
        }

        // Actualizar campos
        habitacion.setNumero(request.getNumero());
        habitacion.setNombreCorto(request.getNombreCorto());
        habitacion.setPrecioBase(request.getPrecioBase());
        habitacion.setCapacidad(request.getCapacidad());
        habitacion.setNumCamas(request.getNumCamas());
        habitacion.setMetrosCuadrados(request.getMetrosCuadrados());

        habitacion = habitacionRepository.save(habitacion);

        // Procesar imágenes en actualización
        if (request.getImagenes() != null && !request.getImagenes().isEmpty()) {
            // Si se envían nuevas imágenes, eliminar las existentes
            habitacionImagenRepository.deleteByHabitacionId(habitacion.getId());
            
            int orden = 0;
            
            // Si hay imagen principal, la guardamos primero
            if (request.getImagen() != null && !request.getImagen().trim().isEmpty()) {
                HabitacionImagen imagenPrincipal = HabitacionImagen.builder()
                        .habitacion(habitacion)
                        .url(request.getImagen())
                        .altText(habitacion.getNumero() + " - " + habitacion.getNombreCorto())
                        .orden(orden++)
                        .esPrincipal(true)
                        .build();
                habitacionImagenRepository.save(imagenPrincipal);
            }
            
            // Guardar las imágenes adicionales
            for (String imagenBase64 : request.getImagenes()) {
                if (imagenBase64 != null && !imagenBase64.trim().isEmpty()) {
                    HabitacionImagen imagen = HabitacionImagen.builder()
                            .habitacion(habitacion)
                            .url(imagenBase64)
                            .altText(habitacion.getNumero() + " - Imagen adicional")
                            .orden(orden++)
                            .esPrincipal(request.getImagen() == null && orden == 1)
                            .build();
                    habitacionImagenRepository.save(imagen);
                }
            }
        } else if (request.getImagen() != null && !request.getImagen().trim().isEmpty()) {
            // Solo actualizar imagen principal si no hay array de imágenes
            final Habitacion habitacionFinal = habitacion;
            final String imagenUrl = request.getImagen();
            habitacionImagenRepository.findImagenPrincipalByHabitacionId(habitacion.getId())
                    .ifPresentOrElse(
                            imagenExistente -> {
                                imagenExistente.setUrl(imagenUrl);
                                habitacionImagenRepository.save(imagenExistente);
                            },
                            () -> {
                                HabitacionImagen nuevaImagen = HabitacionImagen.builder()
                                        .habitacion(habitacionFinal)
                                        .url(imagenUrl)
                                        .altText(habitacionFinal.getNumero() + " - " + habitacionFinal.getNombreCorto())
                                        .orden(0)
                                        .esPrincipal(true)
                                        .build();
                                habitacionImagenRepository.save(nuevaImagen);
                            }
                    );
        }

        // Forzar el flush para asegurar que los cambios estén persistidos
        habitacionRepository.flush();
        
        // Actualizar rango de precios del hotel
        hotelService.actualizarRangoPreciosHotel(habitacion.getHotel().getId());

        return mapToResponse(habitacion);
    }

    /**
     * Cambiar estado de habitación
     */
    @Transactional
    public HabitacionResponse cambiarEstado(Long id, String nuevoEstado, Long propietarioId) {
        Habitacion habitacion = habitacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Habitacion", "id", id));

        // Verificar permisos
        if (!habitacion.getHotel().getPropietario().getId().equals(propietarioId)) {
            throw new BusinessException("No tiene permisos para cambiar el estado de esta habitación");
        }

        habitacion.setEstado(nuevoEstado);
        habitacion = habitacionRepository.save(habitacion);

        return mapToResponse(habitacion);
    }

    /**
     * Eliminar habitación (soft delete)
     */
    @Transactional
    public void eliminarHabitacion(Long id, Long propietarioId) {
        Habitacion habitacion = habitacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Habitacion", "id", id));

        // Verificar permisos
        if (!habitacion.getHotel().getPropietario().getId().equals(propietarioId)) {
            throw new BusinessException("No tiene permisos para eliminar esta habitación");
        }

        habitacionRepository.softDelete(id, LocalDateTime.now());

        // Forzar el flush para asegurar que la eliminación esté persistida
        habitacionRepository.flush();
        
        // Actualizar rango de precios del hotel
        hotelService.actualizarRangoPreciosHotel(habitacion.getHotel().getId());
    }

    /**
     * Mapear entidad a DTO de respuesta
     */
    private HabitacionResponse mapToResponse(Habitacion habitacion) {
        // Obtener imágenes de la habitación
        List<HabitacionImagen> imagenesEntidad = habitacionImagenRepository.findByHabitacionIdOrderByOrdenAsc(habitacion.getId());
        String imagenPrincipal = imagenesEntidad.stream()
                .filter(HabitacionImagen::getEsPrincipal)
                .findFirst()
                .map(HabitacionImagen::getUrl)
                .orElse(null);
        
        List<String> imagenes = imagenesEntidad.stream()
                .map(HabitacionImagen::getUrl)
                .collect(Collectors.toList());

        HabitacionResponse.HabitacionResponseBuilder builder = HabitacionResponse.builder()
                .id(habitacion.getId())
                .numero(habitacion.getNumero())
                .nombreCorto(habitacion.getNombreCorto())
                .precioBase(habitacion.getPrecioBase())
                .capacidad(habitacion.getCapacidad())
                .numCamas(habitacion.getNumCamas())
                .metrosCuadrados(habitacion.getMetrosCuadrados())
                .estado(habitacion.getEstado())
                .imagenPrincipal(imagenPrincipal)
                .imagenes(imagenes)
                .creadoEn(habitacion.getCreadoEn());

        // Tipo de habitación
        if (habitacion.getRoomType() != null) {
            builder.tipoHabitacion(HabitacionResponse.TipoHabitacionDTO.builder()
                    .id(habitacion.getRoomType().getId())
                    .nombre(habitacion.getRoomType().getNombre())
                    .descripcion(habitacion.getRoomType().getDescripcion())
                    .build());
        }

        return builder.build();
    }
}
