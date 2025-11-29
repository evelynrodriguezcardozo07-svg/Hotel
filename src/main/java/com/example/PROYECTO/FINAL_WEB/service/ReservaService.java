package com.example.PROYECTO.FINAL_WEB.service;

import com.example.PROYECTO.FINAL_WEB.dto.request.ReservaRequest;
import com.example.PROYECTO.FINAL_WEB.dto.response.PageResponse;
import com.example.PROYECTO.FINAL_WEB.dto.response.ReservaResponse;
import com.example.PROYECTO.FINAL_WEB.entity.*;
import com.example.PROYECTO.FINAL_WEB.exception.BusinessException;
import com.example.PROYECTO.FINAL_WEB.exception.ResourceNotFoundException;
import com.example.PROYECTO.FINAL_WEB.repository.*;
import com.example.PROYECTO.FINAL_WEB.util.AppConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ReservaService {

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private HabitacionRepository habitacionRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private DisponibilidadService disponibilidadService;
    
    @Autowired
    private CuponService cuponService;

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public ReservaResponse crearReserva(ReservaRequest request, Long usuarioId) {
        // Validar fechas básicas
        boolean esReservaPorHoras = Boolean.TRUE.equals(request.getReservaPorHoras());
        
        if (esReservaPorHoras) {
            // Validación para reservas por horas (day use)
            if (!request.getFechaCheckin().equals(request.getFechaCheckout())) {
                throw new BusinessException("Las reservas por horas deben ser el mismo día");
            }
            if (request.getHoraCheckin() == null || request.getHoraCheckout() == null) {
                throw new BusinessException("Las horas de check-in y check-out son obligatorias para reservas por horas");
            }
            if (!request.getHoraCheckout().isAfter(request.getHoraCheckin())) {
                throw new BusinessException("La hora de checkout debe ser posterior a la hora de checkin");
            }
            // Mínimo 3 horas, máximo 12 horas
            long horas = java.time.Duration.between(request.getHoraCheckin(), request.getHoraCheckout()).toHours();
            if (horas < 3 || horas > 12) {
                throw new BusinessException("Las reservas por horas deben ser de mínimo 3 y máximo 12 horas");
            }
        } else {
            // Validación para reservas por noche (estándar)
            if (request.getFechaCheckout().isBefore(request.getFechaCheckin()) ||
                request.getFechaCheckout().isEqual(request.getFechaCheckin())) {
                throw new BusinessException("La fecha de checkout debe ser posterior a la fecha de checkin");
            }
        }

        // Validar que no sea en el pasado (ahora permite desde HOY)
        if (request.getFechaCheckin().isBefore(LocalDate.now())) {
            throw new BusinessException("La fecha de checkin no puede ser en el pasado");
        }

        // Verificar que la habitación existe
        Habitacion habitacion = habitacionRepository.findByIdWithDetails(request.getHabitacionId())
                .orElseThrow(() -> new ResourceNotFoundException("Habitacion", "id", request.getHabitacionId()));

        // Verificar que la habitación está disponible
        if (!habitacion.isDisponible()) {
            throw new BusinessException("La habitación no está disponible");
        }

        // Verificar capacidad
        int totalHuespedes = request.getCantidadHuespedes();
        int capacidadTotal = habitacion.getCapacidad();
        if (totalHuespedes > capacidadTotal) {
            throw new BusinessException("La habitación no tiene capacidad suficiente para " + totalHuespedes + " huéspedes");
        }

        // CRÍTICO: Verificar disponibilidad con lock (evita double booking)
        boolean disponible = habitacionRepository.isHabitacionDisponible(
                request.getHabitacionId(),
                request.getFechaCheckin(),
                request.getFechaCheckout()
        );

        if (!disponible) {
            throw new BusinessException("La habitación no está disponible para las fechas seleccionadas");
        }

        // Verificar que no haya reservas solapadas
        List<Reserva> reservasSolapadas = reservaRepository.findReservasSolapadas(
                request.getHabitacionId(),
                request.getFechaCheckin(),
                request.getFechaCheckout()
        );

        if (!reservasSolapadas.isEmpty()) {
            throw new BusinessException("Ya existe una reserva para estas fechas");
        }

        // Obtener usuario
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", usuarioId));

        // Calcular precios según tipo de reserva
        BigDecimal subtotal;
        if (esReservaPorHoras) {
            // Precio por hora = 40% del precio por noche
            long horas = java.time.Duration.between(request.getHoraCheckin(), request.getHoraCheckout()).toHours();
            BigDecimal precioPorHora = habitacion.getPrecioBase().multiply(BigDecimal.valueOf(0.40));
            subtotal = precioPorHora.multiply(BigDecimal.valueOf(horas));
        } else {
            // Precio por noche (estándar)
            long noches = ChronoUnit.DAYS.between(request.getFechaCheckin(), request.getFechaCheckout());
            subtotal = habitacion.getPrecioBase().multiply(BigDecimal.valueOf(noches));
        }
        
        BigDecimal impuestos = subtotal.multiply(BigDecimal.valueOf(0.18)); // 18% IGV
        BigDecimal total = subtotal.add(impuestos);
        
        // Aplicar cupón si existe
        BigDecimal descuento = BigDecimal.ZERO;
        if (request.getCodigoCupon() != null && !request.getCodigoCupon().isBlank()) {
            try {
                descuento = cuponService.aplicarCupon(request.getCodigoCupon(), subtotal);
                total = total.subtract(descuento);
            } catch (Exception e) {
                // Si el cupón no es válido, continuar sin descuento
            }
        }
        
        // Verificar y bloquear disponibilidad
        if (!esReservaPorHoras) {
            disponibilidadService.bloquearFechas(
                habitacion.getId(),
                request.getFechaCheckin(),
                request.getFechaCheckout()
            );
        }

        // Generar código único de reserva
        String codigoReserva = AppConstants.PREFIX_CODIGO_RESERVA + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        // Crear reserva
        Reserva reserva = Reserva.builder()
                .codigoReserva(codigoReserva)
                .usuario(usuario)
                .habitacion(habitacion)
                .fechaCheckin(request.getFechaCheckin())
                .fechaCheckout(request.getFechaCheckout())
                .horaCheckin(request.getHoraCheckin())
                .horaCheckout(request.getHoraCheckout())
                .reservaPorHoras(esReservaPorHoras)
                .cantidadHuespedes(request.getCantidadHuespedes())
                .subtotal(subtotal)
                .impuestos(impuestos)
                .total(total)
                .estado(AppConstants.RESERVA_PENDIENTE)
                .notasEspeciales(request.getNotasEspeciales())
                .nombreHuesped(request.getNombreHuesped())
                .apellidoHuesped(request.getApellidoHuesped())
                .dniHuesped(request.getDniHuesped())
                .telefonoHuesped(request.getTelefonoHuesped())
                .build();

        reserva = reservaRepository.save(reserva);

        return mapToResponse(reserva);
    }

    @Transactional(readOnly = true)
    public ReservaResponse getReservaById(Long id) {
        Reserva reserva = reservaRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva", "id", id));

        return mapToResponse(reserva);
    }

    /**
     * Obtener reserva por código
     */
    @Transactional(readOnly = true)
    public ReservaResponse getReservaByCodigo(String codigoReserva) {
        Reserva reserva = reservaRepository.findByCodigoReserva(codigoReserva)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva", "codigoReserva", codigoReserva));

        return mapToResponse(reserva);
    }

    /**
     * Obtener reservas de un usuario con paginación
     */
    @Transactional(readOnly = true)
    public PageResponse<ReservaResponse> getReservasByUsuario(Long usuarioId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("fechaCheckin").descending());
        Page<Reserva> reservaPage = reservaRepository.findByUsuarioId(usuarioId, pageable);

        List<ReservaResponse> content = reservaPage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PageResponse.<ReservaResponse>builder()
                .content(content)
                .pageNumber(reservaPage.getNumber())
                .pageSize(reservaPage.getSize())
                .totalElements(reservaPage.getTotalElements())
                .totalPages(reservaPage.getTotalPages())
                .last(reservaPage.isLast())
                .first(reservaPage.isFirst())
                .empty(reservaPage.isEmpty())
                .build();
    }

    /**
     * Obtener reservas activas de un usuario
     */
    @Transactional(readOnly = true)
    public List<ReservaResponse> getReservasActivas(Long usuarioId) {
        return reservaRepository.findReservasActivasByUsuarioId(usuarioId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtener reservas de un hotel (para propietario)
     */
    @Transactional(readOnly = true)
    public PageResponse<ReservaResponse> getReservasByHotel(Long hotelId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("fechaCheckin").descending());
        Page<Reserva> reservaPage = reservaRepository.findByHotelId(hotelId, pageable);

        List<ReservaResponse> content = reservaPage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PageResponse.<ReservaResponse>builder()
                .content(content)
                .pageNumber(reservaPage.getNumber())
                .pageSize(reservaPage.getSize())
                .totalElements(reservaPage.getTotalElements())
                .totalPages(reservaPage.getTotalPages())
                .last(reservaPage.isLast())
                .first(reservaPage.isFirst())
                .empty(reservaPage.isEmpty())
                .build();
    }

    /**
     * Confirmar reserva
     */
    @Transactional
    public ReservaResponse confirmarReserva(Long id) {
        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva", "id", id));

        if (!reserva.getEstado().equals(AppConstants.RESERVA_PENDIENTE)) {
            throw new BusinessException("Solo se pueden confirmar reservas pendientes");
        }

        reservaRepository.actualizarEstado(id, AppConstants.RESERVA_CONFIRMADA, LocalDateTime.now());
        reserva.setEstado(AppConstants.RESERVA_CONFIRMADA);

        return mapToResponse(reserva);
    }

    /**
     * Cancelar reserva
     */
    @Transactional
    public ReservaResponse cancelarReserva(Long id, String motivo, Long usuarioId) {
        // Cargar reserva con todas las relaciones necesarias
        Reserva reserva = reservaRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva", "id", id));

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", usuarioId));

        // Verificar permisos:
        // 1. El usuario es el dueño de la reserva
        // 2. El usuario es admin
        // 3. El usuario es host del hotel donde está la reserva
        boolean esOwner = reserva.getUsuario().getId().equals(usuarioId);
        boolean esAdmin = usuario.isAdmin();
        
        // Verificar si es host del hotel (con null-safety)
        boolean esHostDelHotel = false;
        try {
            if (reserva.getHabitacion() != null && 
                reserva.getHabitacion().getHotel() != null && 
                reserva.getHabitacion().getHotel().getPropietario() != null) {
                esHostDelHotel = reserva.getHabitacion().getHotel().getPropietario().getId().equals(usuarioId);
            }
        } catch (Exception e) {
            // En caso de error al acceder a las relaciones, continuar con false
            esHostDelHotel = false;
        }

        if (!esOwner && !esAdmin && !esHostDelHotel) {
            throw new BusinessException("No tiene permisos para cancelar esta reserva");
        }

        // Verificar que la reserva se puede cancelar
        if (!reserva.puedeCancelarse()) {
            throw new BusinessException("Esta reserva no puede ser cancelada. Estado actual: " + reserva.getEstado());
        }

        reservaRepository.cancelarReserva(id, LocalDateTime.now(), motivo);
        reserva.setEstado(AppConstants.RESERVA_CANCELADA);
        reserva.setFechaCancelacion(LocalDateTime.now());
        reserva.setMotivoCancelacion(motivo);

        return mapToResponse(reserva);
    }

    /**
     * Completar reserva (después del checkout)
     */
    @Transactional
    public ReservaResponse completarReserva(Long id) {
        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva", "id", id));

        if (!reserva.getEstado().equals(AppConstants.RESERVA_CONFIRMADA)) {
            throw new BusinessException("Solo se pueden completar reservas confirmadas");
        }

        // Verificar que la fecha de checkout ha pasado
        if (reserva.getFechaCheckout().isAfter(LocalDate.now())) {
            throw new BusinessException("No se puede completar una reserva antes de la fecha de checkout");
        }

        reservaRepository.actualizarEstado(id, AppConstants.RESERVA_COMPLETADA, LocalDateTime.now());
        reserva.setEstado(AppConstants.RESERVA_COMPLETADA);

        return mapToResponse(reserva);
    }

    /**
     * Mapear entidad a DTO de respuesta
     */
    private ReservaResponse mapToResponse(Reserva reserva) {
        ReservaResponse.ReservaResponseBuilder builder = ReservaResponse.builder()
                .id(reserva.getId())
                .codigoReserva(reserva.getCodigoReserva())
                .fechaCheckin(reserva.getFechaCheckin())
                .fechaCheckout(reserva.getFechaCheckout())
                .horaCheckin(reserva.getHoraCheckin())
                .horaCheckout(reserva.getHoraCheckout())
                .reservaPorHoras(reserva.getReservaPorHoras())
                .numeroHoras(reserva.calcularNumeroHoras())
                .cantidadHuespedes(reserva.getCantidadHuespedes())
                .subtotal(reserva.getSubtotal())
                .impuestos(reserva.getImpuestos())
                .total(reserva.getTotal())
                .estado(reserva.getEstado())
                .notasEspeciales(reserva.getNotasEspeciales())
                .numeroNoches(reserva.calcularNumeroNoches())
                .nombreHuesped(reserva.getNombreHuesped())
                .apellidoHuesped(reserva.getApellidoHuesped())
                .dniHuesped(reserva.getDniHuesped())
                .telefonoHuesped(reserva.getTelefonoHuesped())
                .puedeCancelarse(reserva.puedeCancelarse())
                .fechaCancelacion(reserva.getFechaCancelacion())
                .motivoCancelacion(reserva.getMotivoCancelacion())
                .creadoEn(reserva.getCreadoEn())
                .actualizadoEn(reserva.getActualizadoEn());

        // Usuario
        if (reserva.getUsuario() != null) {
            builder.usuario(ReservaResponse.UsuarioDTO.builder()
                    .id(reserva.getUsuario().getId())
                    .nombre(reserva.getUsuario().getNombre())
                    .email(reserva.getUsuario().getEmail())
                    .telefono(reserva.getUsuario().getTelefono())
                    .build());
        }

            // Habitación
            if (reserva.getHabitacion() != null) {
                builder.habitacion(ReservaResponse.HabitacionDTO.builder()
                        .id(reserva.getHabitacion().getId())
                        .numero(reserva.getHabitacion().getNumero())
                        .nombreCompleto(reserva.getHabitacion().getNombreCompleto())
                        .build());            // Hotel
            if (reserva.getHabitacion().getHotel() != null) {
                builder.hotel(ReservaResponse.HotelDTO.builder()
                        .id(reserva.getHabitacion().getHotel().getId())
                        .nombre(reserva.getHabitacion().getHotel().getNombre())
                        .build());
            }
        }

        return builder.build();
    }
}
