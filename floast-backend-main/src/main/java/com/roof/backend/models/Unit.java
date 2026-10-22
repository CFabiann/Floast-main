package com.roof.backend.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Unit {
    private String id;
    private String label;
    private String status;
    private Double price;
    private Double area;
    private String description;
    private String notes;
    
    // Aquí reutilizamos la clase Tenant que ya creamos
    private Tenant tenant; 
}