package com.example.PROYECTO.FINAL_WEB.service;

import com.example.PROYECTO.FINAL_WEB.entity.Cupon;
import com.example.PROYECTO.FINAL_WEB.exception.ResourceNotFoundException;
import com.example.PROYECTO.FINAL_WEB.repository.CuponRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class CuponService {

    private final CuponRepository cuponRepository;

    @Transactional(readOnly = true)
    public Cupon validarCupon(String codigo) {
        Cupon cupon = cuponRepository.findByCodigo(codigo.toUpperCase())
            .orElseThrow(() -> new ResourceNotFoundException("Cupón no encontrado"));

        if (!cupon.isValid()) {
            throw new IllegalStateException("Cupón no válido o expirado");
        }

        return cupon;
    }

    @Transactional
    public BigDecimal aplicarCupon(String codigo, BigDecimal montoOriginal) {
        Cupon cupon = validarCupon(codigo);
        BigDecimal descuento = cupon.calcularDescuento(montoOriginal);
        
        if (descuento.compareTo(BigDecimal.ZERO) > 0) {
            cupon.incrementarUsos();
            cuponRepository.save(cupon);
            log.info("✅ Cupón {} aplicado. Descuento: {}", codigo, descuento);
        }
        
        return descuento;
    }
}
