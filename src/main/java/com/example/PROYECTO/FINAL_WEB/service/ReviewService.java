package com.example.PROYECTO.FINAL_WEB.service;

import com.example.PROYECTO.FINAL_WEB.dto.request.ReviewRequest;
import com.example.PROYECTO.FINAL_WEB.dto.response.PageResponse;
import com.example.PROYECTO.FINAL_WEB.dto.response.ReviewResponse;
import com.example.PROYECTO.FINAL_WEB.entity.*;
import com.example.PROYECTO.FINAL_WEB.exception.BusinessException;
import com.example.PROYECTO.FINAL_WEB.exception.ResourceNotFoundException;
import com.example.PROYECTO.FINAL_WEB.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio de gestión de reviews/reseñas
 */
@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ReservaRepository reservaRepository;

    /**
     * Crear review/reseña (solo usuarios con estadía completada)
     */
    @Transactional
    public ReviewResponse crearReview(ReviewRequest request, Long usuarioId) {
        // Verificar que el hotel existe
        Hotel hotel = hotelRepository.findById(request.getHotelId())
                .orElseThrow(() -> new ResourceNotFoundException("Hotel", "id", request.getHotelId()));

        // Verificar que el usuario existe
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", usuarioId));

        // Verificar que el usuario no haya dejado ya una review para este hotel
        if (reviewRepository.existsByUsuarioAndHotel(usuarioId, request.getHotelId())) {
            throw new BusinessException("Ya has dejado una reseña para este hotel");
        }

        // Si se proporciona reserva, validar que existe y esté completada
        Reserva reserva = null;
        boolean verificado = false;
        if (request.getReservaId() != null) {
            reserva = reservaRepository.findById(request.getReservaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Reserva", "id", request.getReservaId()));

            // Verificar que la reserva pertenece al usuario
            if (!reserva.getUsuario().getId().equals(usuarioId)) {
                throw new BusinessException("La reserva no pertenece al usuario");
            }

            // Verificar que la reserva está completada
            if (!"completada".equalsIgnoreCase(reserva.getEstado())) {
                throw new BusinessException("Solo se pueden dejar reseñas de reservas completadas");
            }

            verificado = true;
        } else {
            // Verificar si el usuario tiene una estadía completada en el hotel
            verificado = reservaRepository.hasCompletedStayAtHotel(usuarioId, request.getHotelId());
        }

        // Crear review
        Review review = Review.builder()
                .hotel(hotel)
                .usuario(usuario)
                .reserva(reserva)
                .puntuacion(request.getPuntuacion())
                .puntuacionLimpieza(request.getPuntuacionLimpieza())
                .puntuacionServicio(request.getPuntuacionServicio())
                .puntuacionUbicacion(request.getPuntuacionUbicacion())
                .comentario(request.getComentario())
                .verificado(verificado)
                .utilCount(0)
                .build();

        review = reviewRepository.save(review);

        // Actualizar puntuación promedio del hotel
        Double puntuacionDouble = reviewRepository.calcularPuntuacionPromedio(hotel.getId());
        Long totalReviews = reviewRepository.countByHotelId(hotel.getId());

        BigDecimal puntuacionPromedio = puntuacionDouble != null ? BigDecimal.valueOf(puntuacionDouble) : BigDecimal.ZERO;
        hotel.setPuntuacionPromedio(puntuacionPromedio);
        hotel.setTotalReviews(totalReviews != null ? totalReviews.intValue() : 0);
        hotelRepository.save(hotel);

        return mapToResponse(review);
    }

    /**
     * Obtener reviews de un hotel con paginación
     */
    @Transactional(readOnly = true)
    public PageResponse<ReviewResponse> getReviewsByHotel(Long hotelId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("creadoEn").descending());
        Page<Review> reviewPage = reviewRepository.findByHotelId(hotelId, pageable);

        List<ReviewResponse> content = reviewPage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PageResponse.<ReviewResponse>builder()
                .content(content)
                .pageNumber(reviewPage.getNumber())
                .pageSize(reviewPage.getSize())
                .totalElements(reviewPage.getTotalElements())
                .totalPages(reviewPage.getTotalPages())
                .last(reviewPage.isLast())
                .first(reviewPage.isFirst())
                .empty(reviewPage.isEmpty())
                .build();
    }

    /**
     * Obtener reviews verificadas
     */
    @Transactional(readOnly = true)
    public List<ReviewResponse> getReviewsVerificadas(Long hotelId) {
        List<Review> reviews = reviewRepository.findReviewsVerificadasByHotelId(hotelId);

        return reviews.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtener reviews de un usuario
     */
    @Transactional(readOnly = true)
    public List<ReviewResponse> getReviewsByUsuario(Long usuarioId) {
        return reviewRepository.findByUsuarioId(usuarioId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Responder a una review (solo propietario del hotel)
     */
    @Transactional
    public ReviewResponse responderReview(Long reviewId, String respuesta, Long propietarioId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", reviewId));

        // Verificar que el usuario es el propietario del hotel
        if (!review.getHotel().getPropietario().getId().equals(propietarioId)) {
            throw new BusinessException("No tiene permisos para responder esta reseña");
        }

        reviewRepository.responderReview(reviewId, respuesta, LocalDateTime.now());
        review.setRespuestaHotel(respuesta);
        review.setFechaRespuesta(LocalDateTime.now());

        return mapToResponse(review);
    }

    /**
     * Marcar review como útil
     */
    @Transactional
    public ReviewResponse marcarUtil(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", reviewId));

        reviewRepository.incrementarUtilCount(reviewId);
        review.setUtilCount(review.getUtilCount() + 1);

        return mapToResponse(review);
    }

    /**
     * Eliminar review (soft delete)
     */
    @Transactional
    public void eliminarReview(Long reviewId, Long usuarioId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", reviewId));

        // Verificar que el usuario es el autor o admin
        if (!review.getUsuario().getId().equals(usuarioId)) {
            Usuario usuario = usuarioRepository.findById(usuarioId)
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", usuarioId));
            if (!usuario.isAdmin()) {
                throw new BusinessException("No tiene permisos para eliminar esta reseña");
            }
        }

        reviewRepository.softDelete(reviewId, LocalDateTime.now());

        // Actualizar puntuación del hotel
        Double puntuacionDouble = reviewRepository.calcularPuntuacionPromedio(review.getHotel().getId());
        Long totalReviews = reviewRepository.countByHotelId(review.getHotel().getId());

        Hotel hotel = review.getHotel();
        BigDecimal puntuacionPromedio = puntuacionDouble != null ? BigDecimal.valueOf(puntuacionDouble) : BigDecimal.ZERO;
        hotel.setPuntuacionPromedio(puntuacionPromedio);
        hotel.setTotalReviews(totalReviews != null ? totalReviews.intValue() : 0);
        hotelRepository.save(hotel);
    }

    /**
     * Mapear entidad a DTO de respuesta
     */
    private ReviewResponse mapToResponse(Review review) {
        ReviewResponse.ReviewResponseBuilder builder = ReviewResponse.builder()
                .id(review.getId())
                .puntuacion(review.getPuntuacion())
                .puntuacionLimpieza(review.getPuntuacionLimpieza())
                .puntuacionServicio(review.getPuntuacionServicio())
                .puntuacionUbicacion(review.getPuntuacionUbicacion())
                .puntuacionPromedio(review.calcularPuntuacionPromedio())
                .comentario(review.getComentario())
                .respuestaHotel(review.getRespuestaHotel())
                .verificado(review.getVerificado())
                .utilCount(review.getUtilCount())
                .fechaRespuesta(review.getFechaRespuesta())
                .creadoEn(review.getCreadoEn());

        // Usuario
        if (review.getUsuario() != null) {
            builder.usuario(ReviewResponse.UsuarioDTO.builder()
                    .id(review.getUsuario().getId())
                    .nombre(review.getUsuario().getNombre())
                    .build());
        }

        return builder.build();
    }
}
