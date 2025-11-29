package com.example.PROYECTO.FINAL_WEB.service;

import com.example.PROYECTO.FINAL_WEB.dto.request.HotelRequest;
import com.example.PROYECTO.FINAL_WEB.dto.request.HotelSearchRequest;
import com.example.PROYECTO.FINAL_WEB.dto.response.HotelResponse;
import com.example.PROYECTO.FINAL_WEB.dto.response.PageResponse;
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
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

/**
 * Servicio de gestión de hoteles con transacciones
 */
@Slf4j
@Service
public class HotelService {

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private DireccionRepository direccionRepository;

    @Autowired
    private AmenityRepository amenityRepository;

    @Autowired
    private HabitacionRepository habitacionRepository;

    @Autowired
    private HotelImagenRepository hotelImagenRepository;

    /**
     * Crear nuevo hotel (solo para propietarios)
     */
    @Transactional
    public HotelResponse crearHotel(HotelRequest request, Long propietarioId) {
        // Verificar que el propietario existe y es host
        Usuario propietario = usuarioRepository.findById(propietarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", propietarioId));

        if (!propietario.isHost() && !propietario.isAdmin()) {
            throw new BusinessException("Solo los propietarios pueden crear hoteles");
        }

        // Verificar que no exista un hotel con el mismo nombre
        if (hotelRepository.existsByNombreIgnoreCase(request.getNombre())) {
            throw new BusinessException("Ya existe un hotel con el nombre: " + request.getNombre());
        }

        // Crear dirección si se proporciona
        Direccion direccion = null;
        if (request.getDireccion() != null) {
            direccion = Direccion.builder()
                    .calle(request.getDireccion().getCalle())
                    .ciudad(request.getDireccion().getCiudad())
                    .estadoProvincia(request.getDireccion().getEstadoProvincia())
                    .pais(request.getDireccion().getPais())
                    .codigoPostal(request.getDireccion().getCodigoPostal())
                    .latitud(request.getDireccion().getLatitud())
                    .longitud(request.getDireccion().getLongitud())
                    .build();
            direccion = direccionRepository.save(direccion);
        }

        // Crear hotel
        Hotel hotel = Hotel.builder()
                .propietario(propietario)
                .nombre(request.getNombre())
                .descripcion(request.getDescripcion())
                .direccion(direccion)
                .telefono(request.getTelefono())
                .emailContacto(request.getEmailContacto())
                .estrellas(request.getEstrellas())
                .estado(AppConstants.HOTEL_PENDIENTE)
                .destacado(false)
                .puntuacionPromedio(BigDecimal.ZERO)
                .totalReviews(0)
                .build();

        hotel = hotelRepository.save(hotel);

        // Guardar imagen principal si se proporciona
        if (request.getImagen() != null && !request.getImagen().trim().isEmpty()) {
            HotelImagen imagen = HotelImagen.builder()
                    .hotel(hotel)
                    .url(request.getImagen())
                    .altText(hotel.getNombre())
                    .tipo("portada")
                    .orden(0)
                    .esPrincipal(true)
                    .build();
            hotelImagenRepository.save(imagen);
        }

        // Asociar amenidades si se proporcionan
        if (request.getAmenidadesIds() != null && !request.getAmenidadesIds().isEmpty()) {
            // Implementar lógica de HotelAmenity...
        }

        return mapToResponse(hotel);
    }

    /**
     * Buscar hoteles con filtros y paginación
     */
    @Transactional(readOnly = true)
    public PageResponse<HotelResponse> buscarHoteles(HotelSearchRequest request) {
        Sort sort = Sort.by(
            request.getSortDirection().equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC,
            request.getSortBy()
        );

        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);

        Page<Hotel> hotelPage;

        // Si hay filtros específicos, usar el método de búsqueda complejo
        if (request.getCiudad() != null || request.getPrecioMinimo() != null || 
            request.getEstrellas() != null) {
            hotelPage = hotelRepository.buscarHotelesConFiltros(
                request.getCiudad(),
                request.getPais(),
                request.getEstrellas(),
                request.getPrecioMinimo(),
                request.getPrecioMaximo(),
                pageable
            );
        } else {
            hotelPage = hotelRepository.findHotelesAprobados(pageable);
        }

        List<HotelResponse> content = hotelPage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PageResponse.<HotelResponse>builder()
                .content(content)
                .pageNumber(hotelPage.getNumber())
                .pageSize(hotelPage.getSize())
                .totalElements(hotelPage.getTotalElements())
                .totalPages(hotelPage.getTotalPages())
                .last(hotelPage.isLast())
                .first(hotelPage.isFirst())
                .empty(hotelPage.isEmpty())
                .build();
    }

    /**
     * Obtener hotel por ID con detalles completos
     */
    @Transactional(readOnly = true)
    public HotelResponse getHotelById(Long id) {
        Hotel hotel = hotelRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel", "id", id));

        return mapToResponse(hotel);
    }

    /**
     * Obtener hoteles destacados
     */
    @Transactional(readOnly = true)
    public List<HotelResponse> getHotelesDestacados() {
        return hotelRepository.findHotelesDestacados().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtener hoteles de un propietario
     */
    @Transactional(readOnly = true)
    public List<HotelResponse> getHotelesByPropietario(Long propietarioId) {
        return hotelRepository.findByPropietarioId(propietarioId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Actualizar hotel
     */
    @Transactional
    public HotelResponse actualizarHotel(Long id, HotelRequest request, Long propietarioId) {
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel", "id", id));

        // Verificar que el usuario es el propietario o admin
        if (!hotel.getPropietario().getId().equals(propietarioId)) {
            Usuario usuario = usuarioRepository.findById(propietarioId)
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", propietarioId));
            if (!usuario.isAdmin()) {
                throw new BusinessException("No tiene permisos para actualizar este hotel");
            }
        }

        // Actualizar campos
        hotel.setNombre(request.getNombre());
        hotel.setDescripcion(request.getDescripcion());
        hotel.setTelefono(request.getTelefono());
        hotel.setEmailContacto(request.getEmailContacto());
        hotel.setEstrellas(request.getEstrellas());
        
        // Actualizar imagen principal si se proporciona
        if (request.getImagen() != null && !request.getImagen().isEmpty()) {
            final Hotel hotelFinal = hotel;
            final String imagenUrl = request.getImagen();
            // Buscar imagen principal existente
            hotelImagenRepository.findImagenPrincipalByHotelId(hotel.getId())
                    .ifPresentOrElse(
                            imagenExistente -> {
                                imagenExistente.setUrl(imagenUrl);
                                hotelImagenRepository.save(imagenExistente);
                            },
                            () -> {
                                HotelImagen nuevaImagen = HotelImagen.builder()
                                        .hotel(hotelFinal)
                                        .url(imagenUrl)
                                        .altText(hotelFinal.getNombre())
                                        .tipo("portada")
                                        .orden(0)
                                        .esPrincipal(true)
                                        .build();
                                hotelImagenRepository.save(nuevaImagen);
                            }
                    );
        }

        hotel = hotelRepository.save(hotel);

        // Actualizar rango de precios
        actualizarRangoPreciosHotel(id);

        return mapToResponse(hotel);
    }

    /**
     * Aprobar hotel (solo admin)
     */
    @Transactional
    public HotelResponse aprobarHotel(Long id) {
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel", "id", id));

        hotel.setEstado(AppConstants.HOTEL_APROBADO);
        hotel = hotelRepository.save(hotel);

        return mapToResponse(hotel);
    }

    /**
     * Rechazar hotel (solo admin)
     */
    @Transactional
    public HotelResponse rechazarHotel(Long id) {
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel", "id", id));

        hotel.setEstado(AppConstants.HOTEL_RECHAZADO);
        hotel = hotelRepository.save(hotel);

        return mapToResponse(hotel);
    }

    @Transactional
    public void eliminarHotel(Long id) {
        if (!hotelRepository.existsById(id)) {
            throw new ResourceNotFoundException("Hotel", "id", id);
        }
        hotelRepository.softDelete(id, LocalDateTime.now());
    }

    /**
     * Actualizar rango de precios basado en habitaciones
     */
    @Transactional
    public void actualizarRangoPreciosHotel(Long hotelId) {
        log.info("Actualizando rango de precios para hotel ID: {}", hotelId);
        List<BigDecimal> precios = habitacionRepository.findPreciosByHotelId(hotelId);
        
        if (precios != null && !precios.isEmpty()) {
            BigDecimal min = precios.stream().min(BigDecimal::compareTo).orElse(null);
            BigDecimal max = precios.stream().max(BigDecimal::compareTo).orElse(null);
            log.info("Precio mínimo: {}, Precio máximo: {} (de {} habitaciones)", min, max, precios.size());
            
            if (min != null && max != null) {
                hotelRepository.actualizarRangoPrecios(hotelId, min, max);
                log.info("Rango de precios actualizado correctamente para hotel ID: {}", hotelId);
            } else {
                log.warn("Precio mínimo o máximo es null para hotel ID: {}", hotelId);
            }
        } else {
            log.warn("No se encontraron precios para el hotel ID: {} - El hotel podría no tener habitaciones disponibles", hotelId);
        }
    }

    /**
     * Actualizar precios de todos los hoteles basado en sus habitaciones
     */
    @Transactional
    public void actualizarTodosLosPreciosHoteles() {
        log.info("Iniciando actualización de precios para todos los hoteles...");
        List<Hotel> hoteles = hotelRepository.findAll();
        int actualizados = 0;
        
        for (Hotel hotel : hoteles) {
            try {
                actualizarRangoPreciosHotel(hotel.getId());
                actualizados++;
            } catch (Exception e) {
                log.error("Error al actualizar precios del hotel ID: {}", hotel.getId(), e);
            }
        }
        
        log.info("Actualización completada: {} hoteles procesados de {}", actualizados, hoteles.size());
    }

    /**
     * Mapear entidad a DTO de respuesta
     */
    private HotelResponse mapToResponse(Hotel hotel) {
        // Obtener imágenes del hotel
        List<HotelImagen> imagenesEntidad = hotelImagenRepository.findByHotelIdOrderByOrdenAsc(hotel.getId());
        String imagenPrincipal = imagenesEntidad.stream()
                .filter(HotelImagen::getEsPrincipal)
                .findFirst()
                .map(HotelImagen::getUrl)
                .orElse(null);
        
        List<String> imagenes = imagenesEntidad.stream()
                .map(HotelImagen::getUrl)
                .collect(Collectors.toList());

        HotelResponse.HotelResponseBuilder builder = HotelResponse.builder()
                .id(hotel.getId())
                .nombre(hotel.getNombre())
                .descripcion(hotel.getDescripcion())
                .telefono(hotel.getTelefono())
                .emailContacto(hotel.getEmailContacto())
                .estrellas(hotel.getEstrellas())
                .precioMinimo(hotel.getPrecioMinimo())
                .precioMaximo(hotel.getPrecioMaximo())
                .estado(hotel.getEstado())
                .destacado(hotel.getDestacado())
                .puntuacionPromedio(hotel.getPuntuacionPromedio())
                .totalReviews(hotel.getTotalReviews())
                .imagenPrincipal(imagenPrincipal)
                .imagenes(imagenes)
                .creadoEn(hotel.getCreadoEn())
                .actualizadoEn(hotel.getActualizadoEn());

        // Dirección
        if (hotel.getDireccion() != null) {
            builder.direccion(HotelResponse.DireccionDTO.builder()
                    .id(hotel.getDireccion().getId())
                    .calle(hotel.getDireccion().getCalle())
                    .ciudad(hotel.getDireccion().getCiudad())
                    .estadoProvincia(hotel.getDireccion().getEstadoProvincia())
                    .pais(hotel.getDireccion().getPais())
                    .codigoPostal(hotel.getDireccion().getCodigoPostal())
                    .latitud(hotel.getDireccion().getLatitud())
                    .longitud(hotel.getDireccion().getLongitud())
                    .direccionCompleta(hotel.getDireccion().getDireccionCompleta())
                    .build());
        }

        // Propietario
        if (hotel.getPropietario() != null) {
            builder.propietario(HotelResponse.PropietarioDTO.builder()
                    .id(hotel.getPropietario().getId())
                    .nombre(hotel.getPropietario().getNombre())
                    .email(hotel.getPropietario().getEmail())
                    .build());
        }

        // Amenidades
        if (hotel.getHotelAmenities() != null && !hotel.getHotelAmenities().isEmpty()) {
            List<HotelResponse.AmenidadDTO> amenidadesDTO = hotel.getHotelAmenities().stream()
                    .map(ha -> HotelResponse.AmenidadDTO.builder()
                            .id(ha.getAmenity().getId())
                            .nombre(ha.getAmenity().getNombre())
                            .icono(ha.getAmenity().getIcono())
                            .categoria(ha.getAmenity().getCategoria())
                            .esGratuito(ha.getEsGratuito())
                            .detalle(ha.getDetalle())
                            .build())
                    .collect(Collectors.toList());
            builder.amenidades(amenidadesDTO);
        }

        // Reviews (solo las no eliminadas)
        if (hotel.getReviews() != null && !hotel.getReviews().isEmpty()) {
            List<HotelResponse.ReviewDTO> reviewsDTO = hotel.getReviews().stream()
                    .filter(r -> r.getEliminadoEn() == null)
                    .map(r -> HotelResponse.ReviewDTO.builder()
                            .id(r.getId())
                            .usuario(HotelResponse.UsuarioSimpleDTO.builder()
                                    .id(r.getUsuario().getId())
                                    .nombre(r.getUsuario().getNombre())
                                    .build())
                            .puntuacion(r.getPuntuacion())
                            .comentario(r.getComentario())
                            .respuesta(r.getRespuestaHotel())
                            .creadoEn(r.getCreadoEn())
                            .build())
                    .collect(Collectors.toList());
            builder.reviews(reviewsDTO);
        }

        return builder.build();
    }

    /**
     * Crear hotel por propietario
     */
    @Transactional
    public HotelResponse crearHotelPorPropietario(Map<String, Object> hotelData, Long propietarioId) {
        Usuario propietario = usuarioRepository.findById(propietarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        // Crear dirección
        Map<String, Object> direccionData = (Map<String, Object>) hotelData.get("direccion");
        Direccion direccion = new Direccion();
        direccion.setCalle((String) direccionData.get("calle"));
        direccion.setCiudad((String) direccionData.get("ciudad"));
        direccion.setEstadoProvincia((String) direccionData.get("estadoProvincia"));
        direccion.setPais((String) direccionData.get("pais"));
        direccion.setCodigoPostal((String) direccionData.get("codigoPostal"));
        direccion = direccionRepository.save(direccion);

        // Crear hotel
        Hotel hotel = new Hotel();
        hotel.setNombre((String) hotelData.get("nombre"));
        hotel.setDescripcion((String) hotelData.get("descripcion"));
        hotel.setTelefono((String) hotelData.get("telefono"));
        hotel.setEmailContacto((String) hotelData.get("emailContacto"));
        
        // Convertir estrellas de manera segura (puede venir como String o Number)
        Object estrellasObj = hotelData.get("estrellas");
        Integer estrellas = null;
        if (estrellasObj instanceof Number) {
            estrellas = ((Number) estrellasObj).intValue();
        } else if (estrellasObj instanceof String) {
            estrellas = Integer.parseInt((String) estrellasObj);
        }
        hotel.setEstrellas(estrellas);
        
        hotel.setDireccion(direccion);
        hotel.setPropietario(propietario);
        hotel.setEstado("pendiente"); // Por defecto pendiente
        hotel.setDestacado((Boolean) hotelData.getOrDefault("destacado", false));
        hotel.setPrecioMinimo(BigDecimal.ZERO);
        hotel.setPrecioMaximo(BigDecimal.ZERO);
        hotel.setPuntuacionPromedio(BigDecimal.ZERO);
        hotel.setTotalReviews(0);

        hotel = hotelRepository.save(hotel);

        // Guardar imagen principal si se proporciona
        if (hotelData.containsKey("imagen") && hotelData.get("imagen") != null) {
            String imagenBase64 = (String) hotelData.get("imagen");
            if (!imagenBase64.trim().isEmpty()) {
                HotelImagen imagen = HotelImagen.builder()
                        .hotel(hotel)
                        .url(imagenBase64)
                        .altText(hotel.getNombre())
                        .tipo("portada")
                        .orden(0)
                        .esPrincipal(true)
                        .build();
                hotelImagenRepository.save(imagen);
            }
        }

        return mapToResponse(hotel);
    }

    /**
     * Obtener hoteles por propietario
     */
    @Transactional(readOnly = true)
    public List<HotelResponse> obtenerHotelesPorPropietario(Long propietarioId) {
        List<Hotel> hoteles = hotelRepository.findByPropietarioIdAndEliminadoEnIsNull(propietarioId);
        return hoteles.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    /**
     * Actualizar hotel por propietario
     */
    @Transactional
    public HotelResponse actualizarHotelPorPropietario(Long hotelId, Map<String, Object> hotelData, Long propietarioId) {
        Hotel hotel = hotelRepository.findByIdAndEliminadoEnIsNull(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel no encontrado"));

        if (!hotel.getPropietario().getId().equals(propietarioId)) {
            throw new BusinessException("No tienes permiso para editar este hotel");
        }

        hotel.setNombre((String) hotelData.get("nombre"));
        hotel.setDescripcion((String) hotelData.get("descripcion"));
        hotel.setTelefono((String) hotelData.get("telefono"));
        hotel.setEmailContacto((String) hotelData.get("emailContacto"));
        
        // Convertir estrellas de manera segura
        Object estrellasObj = hotelData.get("estrellas");
        if (estrellasObj instanceof Number) {
            hotel.setEstrellas(((Number) estrellasObj).intValue());
        } else if (estrellasObj instanceof String) {
            hotel.setEstrellas(Integer.parseInt((String) estrellasObj));
        }

        // Actualizar imagen si se proporciona
        if (hotelData.containsKey("imagen") && hotelData.get("imagen") != null) {
            String imagenBase64 = (String) hotelData.get("imagen");
            if (!imagenBase64.trim().isEmpty()) {
                final Hotel hotelFinal = hotel;
                final String imagenUrl = imagenBase64;
                hotelImagenRepository.findImagenPrincipalByHotelId(hotel.getId())
                        .ifPresentOrElse(
                                imagenExistente -> {
                                    imagenExistente.setUrl(imagenUrl);
                                    hotelImagenRepository.save(imagenExistente);
                                },
                                () -> {
                                    HotelImagen nuevaImagen = HotelImagen.builder()
                                            .hotel(hotelFinal)
                                            .url(imagenUrl)
                                            .altText(hotelFinal.getNombre())
                                            .tipo("portada")
                                            .orden(0)
                                            .esPrincipal(true)
                                            .build();
                                    hotelImagenRepository.save(nuevaImagen);
                                }
                        );
            }
        }

        if (hotelData.containsKey("direccion")) {
            Map<String, Object> direccionData = (Map<String, Object>) hotelData.get("direccion");
            Direccion direccion = hotel.getDireccion();
            direccion.setCalle((String) direccionData.get("calle"));
            direccion.setCiudad((String) direccionData.get("ciudad"));
            direccion.setEstadoProvincia((String) direccionData.get("estadoProvincia"));
            direccion.setPais((String) direccionData.get("pais"));
            direccion.setCodigoPostal((String) direccionData.get("codigoPostal"));
        }

        hotel = hotelRepository.save(hotel);
        return mapToResponse(hotel);
    }

    /**
     * Eliminar hotel por propietario
     */
    @Transactional
    public void eliminarHotelPorPropietario(Long hotelId, Long propietarioId) {
        Hotel hotel = hotelRepository.findByIdAndEliminadoEnIsNull(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel no encontrado"));

        if (!hotel.getPropietario().getId().equals(propietarioId)) {
            throw new BusinessException("No tienes permiso para eliminar este hotel");
        }

        hotel.setEliminadoEn(LocalDateTime.now());
        hotelRepository.save(hotel);
    }

    /**
     * Obtener hoteles pendientes (admin)
     */
    @Transactional(readOnly = true)
    public List<HotelResponse> obtenerHotelesPendientes() {
        List<Hotel> hoteles = hotelRepository.findByEstadoAndEliminadoEnIsNull("pendiente");
        return hoteles.stream().map(this::mapToResponse).collect(Collectors.toList());
    }
}
