package com.roof.backend.controllers;

import com.roof.backend.models.Unit;
import com.roof.backend.services.UnitService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/properties")
@CrossOrigin(origins = "*") // Permite que tu frontend acceda a la API
public class UnitController {

    @Autowired
    private UnitService unitService;

    // Esta ruta coincide con la que debe llamar tu updateUnit() en JavaScript
    // Ejemplo: PUT /api/properties/prop_1/units/hab_1
    @PutMapping("/{propId}/units/{unitId}")
    public ResponseEntity<Unit> updateUnit(
            @PathVariable String propId, 
            @PathVariable String unitId, 
            @RequestBody Unit updates) {
        
        try {
            Unit updatedUnit = unitService.updateUnit(propId, unitId, updates);
            return ResponseEntity.ok(updatedUnit);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
    
    
    @GetMapping("/{propId}/units")
    public ResponseEntity<List<Unit>> getUnits(@PathVariable String propId) {
        try {
            List<Unit> units = unitService.getUnitsByPropertyId(propId);
            return ResponseEntity.ok(units);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}