package com.example.PROYECTO.FINAL_WEB.repository;

import com.example.PROYECTO.FINAL_WEB.entity.RoomAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface RoomAvailabilityRepository extends JpaRepository<RoomAvailability, Long> {
    
    List<RoomAvailability> findByHabitacionIdAndFechaBetween(
        Long habitacionId, LocalDate inicio, LocalDate fin
    );
    
    List<RoomAvailability> findByHabitacionIdAndFechaBetweenAndEstado(
        Long habitacionId, LocalDate inicio, LocalDate fin, String estado
    );
    
    Optional<RoomAvailability> findByHabitacionIdAndFecha(Long habitacionId, LocalDate fecha);
}
