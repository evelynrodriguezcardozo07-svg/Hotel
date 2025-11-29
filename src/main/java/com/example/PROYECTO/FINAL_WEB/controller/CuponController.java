package com.example.PROYECTO.FINAL_WEB.controller;

import com.example.PROYECTO.FINAL_WEB.dto.response.ApiResponse;
import com.example.PROYECTO.FINAL_WEB.entity.Cupon;
import com.example.PROYECTO.FINAL_WEB.service.CuponService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cupones")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "http://localhost:4200")
public class CuponController {

    private final CuponService cuponService;

    @GetMapping("/validar/{codigo}")
    @PreAuthorize("hasAnyRole('GUEST', 'HOST', 'ADMIN')")
    public ResponseEntity<ApiResponse<Cupon>> validarCupon(@PathVariable String codigo) {
        try {
            Cupon cupon = cuponService.validarCupon(codigo);
            return ResponseEntity.ok(ApiResponse.success(cupon, "Cupón válido"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }
}
