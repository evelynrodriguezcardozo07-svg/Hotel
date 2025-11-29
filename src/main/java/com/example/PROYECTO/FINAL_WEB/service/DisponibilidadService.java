package com.example.PROYECTO.FINAL_WEB.service;

import com.example.PROYECTO.FINAL_WEB.entity.Habitacion;
import com.example.PROYECTO.FINAL_WEB.entity.RoomAvailability;
import com.example.PROYECTO.FINAL_WEB.repository.HabitacionRepository;
import com.example.PROYECTO.FINAL_WEB.repository.RoomAvailabilityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class DisponibilidadService {

    private final RoomAvailabilityRepository availabilityRepository;
    private final HabitacionRepository habitacionRepository;

    @Transactional(readOnly = true)
    public Map<LocalDate, BigDecimal> obtenerPreciosPorFecha(Long habitacionId, LocalDate inicio, LocalDate fin) {
        Map<LocalDate, BigDecimal> precios = new HashMap<>();
        
        Habitacion habitacion = habitacionRepository.findById(habitacionId)
            .orElseThrow(() -> new RuntimeException("Habitación no encontrada"));
        
        List<RoomAvailability> disponibilidades = availabilityRepository
            .findByHabitacionIdAndFechaBetween(habitacionId, inicio, fin);
        
        Map<LocalDate, RoomAvailability> dispMap = new HashMap<>();
        disponibilidades.forEach(d -> dispMap.put(d.getFecha(), d));
        
        LocalDate current = inicio;
        while (!current.isAfter(fin)) {
            RoomAvailability disp = dispMap.get(current);
            BigDecimal precio = disp != null && disp.getPrecioDia() != null ? 
                disp.getPrecioDia() : habitacion.getPrecioBase();
            
            precios.put(current, precio);
            current = current.plusDays(1);
        }
        
        return precios;
    }

    @Transactional(readOnly = true)
    public boolean estaDisponible(Long habitacionId, LocalDate inicio, LocalDate fin) {
        List<RoomAvailability> bloqueadas = availabilityRepository
            .findByHabitacionIdAndFechaBetweenAndEstado(habitacionId, inicio, fin, "reservado");
        
        return bloqueadas.isEmpty();
    }

    @Transactional
    public void bloquearFechas(Long habitacionId, LocalDate inicio, LocalDate fin) {
        LocalDate current = inicio;
        while (!current.isAfter(fin)) {
            RoomAvailability availability = availabilityRepository
                .findByHabitacionIdAndFecha(habitacionId, current)
                .orElse(RoomAvailability.builder()
                    .habitacion(habitacionRepository.findById(habitacionId).orElseThrow())
                    .fecha(current)
                    .build());
            
            availability.setEstado("reservado");
            availabilityRepository.save(availability);
            
            current = current.plusDays(1);
        }
    }

    @Transactional
    public void liberarFechas(Long habitacionId, LocalDate inicio, LocalDate fin) {
        LocalDate current = inicio;
        while (!current.isAfter(fin)) {
            availabilityRepository.findByHabitacionIdAndFecha(habitacionId, current)
                .ifPresent(availability -> {
                    availability.setEstado("disponible");
                    availabilityRepository.save(availability);
                });
            
            current = current.plusDays(1);
        }
    }
}
