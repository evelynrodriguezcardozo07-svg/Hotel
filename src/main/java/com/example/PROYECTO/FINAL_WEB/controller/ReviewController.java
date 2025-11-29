package com.example.PROYECTO.FINAL_WEB.controller;

import com.example.PROYECTO.FINAL_WEB.dto.request.ReviewRequest;
import com.example.PROYECTO.FINAL_WEB.dto.response.ApiResponse;
import com.example.PROYECTO.FINAL_WEB.dto.response.PageResponse;
import com.example.PROYECTO.FINAL_WEB.dto.response.ReviewResponse;
import com.example.PROYECTO.FINAL_WEB.entity.Usuario;
import com.example.PROYECTO.FINAL_WEB.service.AuthService;
import com.example.PROYECTO.FINAL_WEB.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller de reviews/reseñas
 */
@RestController
@RequestMapping("/api/reviews")
@CrossOrigin(origins = "http://localhost:4200", maxAge = 3600)
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private AuthService authService;

    /**
     * POST /api/reviews - Crear review
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ReviewResponse>> crearReview(@Valid @RequestBody ReviewRequest request) {
        Usuario usuario = authService.getCurrentUser();
        ReviewResponse response = reviewService.crearReview(request, usuario.getId());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Reseña creada exitosamente"));
    }

    /**
     * GET /api/reviews/hotel/{hotelId} - Obtener reviews de un hotel
     */
    @GetMapping("/hotel/{hotelId}")
    public ResponseEntity<ApiResponse<PageResponse<ReviewResponse>>> getReviewsByHotel(
            @PathVariable Long hotelId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResponse<ReviewResponse> response = reviewService.getReviewsByHotel(hotelId, page, size);
        return ResponseEntity.ok(ApiResponse.success(response, "Reviews del hotel"));
    }

    /**
     * GET /api/reviews/hotel/{hotelId}/verificadas - Obtener reviews verificadas
     */
    @GetMapping("/hotel/{hotelId}/verificadas")
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> getReviewsVerificadas(@PathVariable Long hotelId) {
        List<ReviewResponse> response = reviewService.getReviewsVerificadas(hotelId);
        return ResponseEntity.ok(ApiResponse.success(response, "Reviews verificadas"));
    }

    /**
     * GET /api/reviews/mis-reviews - Obtener reviews del usuario actual
     */
    @GetMapping("/mis-reviews")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> getMisReviews() {
        Usuario usuario = authService.getCurrentUser();
        List<ReviewResponse> response = reviewService.getReviewsByUsuario(usuario.getId());
        return ResponseEntity.ok(ApiResponse.success(response, "Mis reseñas"));
    }

    /**
     * POST /api/reviews/{id}/responder - Responder a una review (propietario)
     */
    @PostMapping("/{id}/responder")
    @PreAuthorize("hasAnyRole('HOST', 'ADMIN')")
    public ResponseEntity<ApiResponse<ReviewResponse>> responderReview(
            @PathVariable Long id,
            @RequestParam String respuesta) {
        Usuario usuario = authService.getCurrentUser();
        ReviewResponse response = reviewService.responderReview(id, respuesta, usuario.getId());
        return ResponseEntity.ok(ApiResponse.success(response, "Respuesta agregada"));
    }

    /**
     * POST /api/reviews/{id}/util - Marcar review como útil
     */
    @PostMapping("/{id}/util")
    public ResponseEntity<ApiResponse<ReviewResponse>> marcarUtil(@PathVariable Long id) {
        ReviewResponse response = reviewService.marcarUtil(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Marcado como útil"));
    }

    /**
     * DELETE /api/reviews/{id} - Eliminar review
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<String>> eliminarReview(@PathVariable Long id) {
        Usuario usuario = authService.getCurrentUser();
        reviewService.eliminarReview(id, usuario.getId());
        return ResponseEntity.ok(ApiResponse.success("Eliminada", "Reseña eliminada"));
    }
}
