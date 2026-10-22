package com.roof.backend.controllers;

import com.roof.backend.models.Property;
import com.roof.backend.services.PropertyService;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController 
@RequestMapping("/api/properties") 
@CrossOrigin(origins = "*") 
public class PropertyController {

    @Autowired
    private PropertyService propertyService;

    
    // Este método escucha peticiones GET
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserProperties(@PathVariable String userId) {
        try {
            List<Property> properties = propertyService.getPropertiesByUserId(userId);
            return ResponseEntity.ok(properties);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener las propiedades: " + e.getMessage());
        }
    }
    // Este método escucha peticiones POST en http://localhost:8080/api/properties
    @PostMapping
    
    public ResponseEntity<?> createProperty(@RequestBody Property property) {
        try {
            // 1. Validar que el frontend nos haya mandado quién es el usuario
            if (property.getUserId() == null || property.getUserId().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("El ID del usuario es obligatorio.");
            }

            // 2. Mandar a guardar la propiedad a nuestro servicio
            Property savedProperty = propertyService.createProperty(property, property.getUserId());
            
            // 3. Si todo sale bien, devolvemos un status 200 (OK) y los datos de la propiedad
            return ResponseEntity.ok(savedProperty);

        } catch (RuntimeException e) {
           
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
            
        } catch (Exception e) {
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno del servidor: " + e.getMessage());
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getPropertyById(@PathVariable String id) {
        try {
            Property property = propertyService.getPropertyById(id);
            if (property != null) {
                return ResponseEntity.ok(property);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Propiedad no encontrada");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener la propiedad: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateProperty(@PathVariable String id, @RequestBody Property updates) {
        try {
            String userId = updates != null ? updates.getUserId() : null;
            Property updated = propertyService.updateProperty(id, updates, userId);
            if (updated != null) {
                return ResponseEntity.ok(updated);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Propiedad no encontrada");
            }
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al actualizar la propiedad: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProperty(@PathVariable String id, @RequestParam(required = false) String userId) {
        try {
            boolean deleted = propertyService.deleteProperty(id, userId);
            if (deleted) {
                return ResponseEntity.ok("Propiedad eliminada");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Propiedad no encontrada");
            }
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al eliminar la propiedad: " + e.getMessage());
        }
    }
    
    @GetMapping("/public")
    public ResponseEntity<Map<String, Object>> getPublicProperties(
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lng,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String rentMode,
            @RequestParam(required = false) String buildingType,
            @RequestParam(required = false) Boolean petsAllowed,
            @RequestParam(required = false) Boolean childrenAllowed,
            @RequestParam(required = false) List<String> amenities,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int size) {
        try {
            Map<String, Object> response = propertyService.getPublicProperties(
                    lat, lng, type, rentMode, buildingType, petsAllowed, childrenAllowed, amenities, page, size);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}