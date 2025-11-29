package com.example.PROYECTO.FINAL_WEB.repository;

import com.example.PROYECTO.FINAL_WEB.entity.Direccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository para Direccion
 */
@Repository
public interface DireccionRepository extends JpaRepository<Direccion, Long> {
}
