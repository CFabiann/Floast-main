package com.roof.backend.models;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Property {
    private String id;
    private String userId;
    private String name;
    private PropertyType type;
    private String buildingType;
    private String rentMode;
    private String location; 
    private Double price;
    private String status;
    private String description;
    private Integer habCount;
    private Integer localCount;
    private Tenant tenant;
    private Double latitude;
    private Double longitude;
    
    private List<String> amenities;
    private Policies policies;
    
    public Tenant getTenant() { 
    return tenant; 
}

public void setTenant(Tenant tenant) { 
    this.tenant = tenant; 
}

@Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Policies {
        private Boolean petsAllowed;
        private Boolean childrenAllowed;
    }

}