package com.example.PROYECTO.FINAL_WEB.service;

import com.example.PROYECTO.FINAL_WEB.dto.request.PagoRequest;
import com.example.PROYECTO.FINAL_WEB.dto.response.PagoResponse;
import com.example.PROYECTO.FINAL_WEB.entity.*;
import com.example.PROYECTO.FINAL_WEB.exception.ResourceNotFoundException;
import com.example.PROYECTO.FINAL_WEB.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Servicio para procesar pagos con Culqi
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PagoService {

    private final PagoRepository pagoRepository;
    private final ReservaRepository reservaRepository;
    private final RestTemplate restTemplate;

    @Value("${culqi.secret.key:sk_test_dummy}")
    private String culqiSecretKey;

    @Value("${culqi.api.url:https://api.culqi.com/v2}")
    private String culqiApiUrl;

    /**
     * Procesa un pago con Culqi
     */
    @Transactional
    public PagoResponse procesarPago(PagoRequest request) {
        log.info("🔄 Procesando pago para reserva: {}", request.getReservaId());

        // 1. Validar reserva
        Reserva reserva = reservaRepository.findById(request.getReservaId())
            .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada"));

        if (!"pendiente".equalsIgnoreCase(reserva.getEstado())) {
            throw new IllegalStateException("La reserva ya fue procesada");
        }

        // 2. Validar monto
        if (request.getMonto().compareTo(reserva.getTotal()) != 0) {
            throw new IllegalArgumentException("El monto no coincide con el total de la reserva");
        }

        // 3. Crear cargo en Culqi
        Map<String, Object> culqiResponse;
        try {
            culqiResponse = crearCargoEnCulqi(request, reserva);
        } catch (Exception e) {
            log.error("❌ Error al procesar pago con Culqi: {}", e.getMessage());
            return crearPagoFallido(reserva, request, e.getMessage());
        }

        // 4. Guardar pago en BD
        Pago pago = Pago.builder()
            .reserva(reserva)
            .monto(request.getMonto())
            .moneda(request.getMoneda())
            .metodo(request.getMetodo())
            .estado("completado")
            .transaccionId((String) culqiResponse.get("id"))
            .proveedorPago("Culqi")
            .fechaPago(LocalDateTime.now())
            .build();

        pago = pagoRepository.save(pago);

        // 5. Actualizar estado de reserva
        reserva.setEstado("confirmada");
        reservaRepository.save(reserva);

        log.info("✅ Pago procesado exitosamente: {}", pago.getId());

        // 6. Construir respuesta
        return PagoResponse.builder()
            .id(pago.getId())
            .reservaId(reserva.getId())
            .codigoReserva(reserva.getCodigoReserva())
            .monto(pago.getMonto())
            .moneda(pago.getMoneda())
            .metodo(pago.getMetodo())
            .estado(pago.getEstado())
            .transaccionId(pago.getTransaccionId())
            .proveedorPago(pago.getProveedorPago())
            .fechaPago(pago.getFechaPago())
            .mensaje("Pago procesado exitosamente")
            .ultimosDigitos(request.getUltimosDigitos())
            .marcaTarjeta(request.getMarcaTarjeta())
            .build();
    }

    /**
     * Crea un cargo en Culqi usando su API
     */
    private Map<String, Object> crearCargoEnCulqi(PagoRequest request, Reserva reserva) {
        String url = culqiApiUrl + "/charges";

        // Headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + culqiSecretKey);

        // Body
        Map<String, Object> body = new HashMap<>();
        body.put("amount", request.getMonto().multiply(new BigDecimal("100")).intValue()); // Culqi usa centavos
        body.put("currency_code", request.getMoneda());
        body.put("email", request.getEmail());
        body.put("source_id", request.getCulqiToken());
        body.put("description", request.getDescripcion() != null ? 
            request.getDescripcion() : 
            "Reserva " + reserva.getCodigoReserva());

        // Metadata adicional
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("reserva_id", reserva.getId());
        metadata.put("codigo_reserva", reserva.getCodigoReserva());
        metadata.put("hotel_id", reserva.getHabitacion().getHotel().getId());
        body.put("metadata", metadata);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        // Llamar API de Culqi
        log.info("📡 Llamando a Culqi API: {}", url);
        ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

        if (response.getStatusCode() == HttpStatus.CREATED || response.getStatusCode() == HttpStatus.OK) {
            log.info("✅ Cargo creado en Culqi: {}", response.getBody());
            return response.getBody();
        } else {
            throw new RuntimeException("Error al crear cargo en Culqi: " + response.getStatusCode());
        }
    }

    /**
     * Crea un registro de pago fallido
     */
    private PagoResponse crearPagoFallido(Reserva reserva, PagoRequest request, String mensajeError) {
        Pago pagoFallido = Pago.builder()
            .reserva(reserva)
            .monto(request.getMonto())
            .moneda(request.getMoneda())
            .metodo(request.getMetodo())
            .estado("fallido")
            .proveedorPago("Culqi")
            .build();

        pagoFallido = pagoRepository.save(pagoFallido);

        return PagoResponse.builder()
            .id(pagoFallido.getId())
            .reservaId(reserva.getId())
            .codigoReserva(reserva.getCodigoReserva())
            .monto(request.getMonto())
            .moneda(request.getMoneda())
            .metodo(request.getMetodo())
            .estado("fallido")
            .mensaje("Error al procesar pago: " + mensajeError)
            .build();
    }

    /**
     * Obtiene el historial de pagos de una reserva
     */
    public List<PagoResponse> obtenerPagosPorReserva(Long reservaId) {
        Reserva reserva = reservaRepository.findById(reservaId)
            .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada"));

        return reserva.getPagos().stream()
            .map(pago -> PagoResponse.builder()
                .id(pago.getId())
                .reservaId(reserva.getId())
                .codigoReserva(reserva.getCodigoReserva())
                .monto(pago.getMonto())
                .moneda(pago.getMoneda())
                .metodo(pago.getMetodo())
                .estado(pago.getEstado())
                .transaccionId(pago.getTransaccionId())
                .proveedorPago(pago.getProveedorPago())
                .fechaPago(pago.getFechaPago())
                .build())
            .toList();
    }

    /**
     * Reembolsa un pago (solo para testing o cancelaciones)
     */
    @Transactional
    public PagoResponse reembolsarPago(Long pagoId) {
        Pago pago = pagoRepository.findById(pagoId)
            .orElseThrow(() -> new ResourceNotFoundException("Pago no encontrado"));

        if (!"completado".equalsIgnoreCase(pago.getEstado())) {
            throw new IllegalStateException("Solo se pueden reembolsar pagos completados");
        }

        // TODO: Implementar lógica de reembolso con API de Culqi
        pago.setEstado("reembolsado");
        pago.setActualizadoEn(LocalDateTime.now());
        pago = pagoRepository.save(pago);

        // Actualizar reserva a cancelada
        Reserva reserva = pago.getReserva();
        reserva.setEstado("cancelada");
        reserva.setFechaCancelacion(LocalDateTime.now());
        reserva.setMotivoCancelacion("Reembolso procesado");
        reservaRepository.save(reserva);

        return PagoResponse.builder()
            .id(pago.getId())
            .reservaId(reserva.getId())
            .estado(pago.getEstado())
            .mensaje("Reembolso procesado exitosamente")
            .build();
    }
}
