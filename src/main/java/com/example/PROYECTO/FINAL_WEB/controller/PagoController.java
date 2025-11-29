package com.example.PROYECTO.FINAL_WEB.controller;

import com.example.PROYECTO.FINAL_WEB.dto.request.PagoRequest;
import com.example.PROYECTO.FINAL_WEB.dto.response.ApiResponse;
import com.example.PROYECTO.FINAL_WEB.dto.response.PagoResponse;
import com.example.PROYECTO.FINAL_WEB.service.PagoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador para gestionar pagos
 */
@RestController
@RequestMapping("/api/pagos")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "http://localhost:4200")
public class PagoController {

    private final PagoService pagoService;

    /**
     * Procesa un pago con Culqi
     * POST /api/pagos/procesar
     */
    @PostMapping("/procesar")
    @PreAuthorize("hasAnyRole('GUEST', 'HOST', 'ADMIN')")
    public ResponseEntity<ApiResponse<PagoResponse>> procesarPago(
        @Valid @RequestBody PagoRequest request
    ) {
        try {
            log.info("📥 Solicitud de pago recibida para reserva: {}", request.getReservaId());
            
            PagoResponse response = pagoService.procesarPago(request);
            
            if ("completado".equals(response.getEstado())) {
                return ResponseEntity.ok(ApiResponse.success(response, "Pago procesado exitosamente"));
            } else {
                return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED)
                    .body(ApiResponse.error(response.getMensaje()));
            }
            
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.error("❌ Error de validación: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("❌ Error al procesar pago: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Error al procesar el pago"));
        }
    }

    /**
     * Obtiene el historial de pagos de una reserva
     * GET /api/pagos/reserva/{reservaId}
     */
    @GetMapping("/reserva/{reservaId}")
    @PreAuthorize("hasAnyRole('GUEST', 'HOST', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<PagoResponse>>> obtenerPagosPorReserva(
        @PathVariable Long reservaId
    ) {
        try {
            List<PagoResponse> pagos = pagoService.obtenerPagosPorReserva(reservaId);
            return ResponseEntity.ok(ApiResponse.success(pagos));
        } catch (Exception e) {
            log.error("❌ Error al obtener pagos: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Error al obtener pagos"));
        }
    }

    /**
     * Reembolsa un pago
     * POST /api/pagos/{pagoId}/reembolsar
     */
    @PostMapping("/{pagoId}/reembolsar")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOST')")
    public ResponseEntity<ApiResponse<PagoResponse>> reembolsarPago(
        @PathVariable Long pagoId
    ) {
        try {
            PagoResponse response = pagoService.reembolsarPago(pagoId);
            return ResponseEntity.ok(ApiResponse.success(response, "Reembolso procesado"));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("❌ Error al reembolsar pago: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Error al procesar reembolso"));
        }
    }
}
