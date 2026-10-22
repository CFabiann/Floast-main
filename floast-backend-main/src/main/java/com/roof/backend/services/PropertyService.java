package com.roof.backend.services;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.roof.backend.models.Property;
import com.roof.backend.models.PropertyType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
public class PropertyService {

    @Autowired
    private Firestore db;

    public Property createProperty(Property property, String userId) throws ExecutionException, InterruptedException {
        // 1. Obtener los datos del usuario para saber qué plan tiene
        DocumentReference userRef = db.collection("users").document(userId);
        ApiFuture<DocumentSnapshot> userQuery = userRef.get();
        DocumentSnapshot userSnapshot = userQuery.get();

        String plan = "SPARK"; // Por defecto
        if (userSnapshot.exists() && userSnapshot.contains("plan")) {
            plan = userSnapshot.getString("plan").toUpperCase();
        }

        // 2. Aplicar la lógica de los límites si el plan es FREE / SPARK
        if ("SPARK".equals(plan)) {
            CollectionReference propertiesRef = db.collection("properties");
            Query query = propertiesRef.whereEqualTo("userId", userId);
            ApiFuture<QuerySnapshot> querySnapshot = query.get();
            List<QueryDocumentSnapshot> documents = querySnapshot.get().getDocuments();

            int casasCount = 0;
            int edificiosCount = 0;

            for (DocumentSnapshot doc : documents) {
                String tipo = doc.getString("type");
                if ("VIVIENDA".equalsIgnoreCase(tipo)) {
                    casasCount++;
                }
                if ("EDIFICIO".equalsIgnoreCase(tipo)) {
                    edificiosCount++;
                }
            }

            if (property.getType() == PropertyType.VIVIENDA && casasCount >= 2) {
                throw new RuntimeException("Límite alcanzado: Tu plan SPARK solo permite 2 vivienda. ¡Sube a Premium!");
            }
            if (property.getType() == PropertyType.EDIFICIO && edificiosCount >= 1) {
                throw new RuntimeException("Límite alcanzado: Tu plan SPARK solo permite 1 edificio. ¡Sube a Premium!");
            }
        }

        // 3. Guardar la propiedad
        property.setUserId(userId);
        if (property.getStatus() == null || property.getStatus().trim().isEmpty()) {
            property.setStatus("disponible");
        }

        CollectionReference propertiesRef = db.collection("properties");
        DocumentReference newPropRef = propertiesRef.document();

        property.setId(newPropRef.getId());

        ApiFuture<WriteResult> result = newPropRef.set(property);
        result.get();

        return property;
    }

    public List<Property> getPropertiesByUserId(String userId) throws ExecutionException, InterruptedException {
        CollectionReference propertiesRef = db.collection("properties");
        Query query = propertiesRef.whereEqualTo("userId", userId);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        List<QueryDocumentSnapshot> documents = querySnapshot.get().getDocuments();

        List<Property> properties = new ArrayList<>();

        for (DocumentSnapshot doc : documents) {
            Property prop = doc.toObject(Property.class);
            if (prop != null) {
                prop.setId(doc.getId());
                properties.add(prop);
            }
        }

        return properties;
    }

    public Property getPropertyById(String id) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection("properties").document(id);
        DocumentSnapshot document = docRef.get().get();

        if (document.exists()) {
            Property prop = document.toObject(Property.class);
            if (prop != null) {
                prop.setId(document.getId());
            }
            return prop;
        }
        return null;
    }

    public Property updateProperty(String id, Property updates, String userId) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection("properties").document(id);
        DocumentSnapshot document = docRef.get().get();

        if (!document.exists()) {
            return null;
        }

        Property existing = document.toObject(Property.class);
        if (existing != null) {
            existing.setId(document.getId());
        }

        if (userId != null && existing != null && existing.getUserId() != null
                && !existing.getUserId().equals(userId)) {
            throw new RuntimeException("No tienes permisos para editar esta propiedad.");
        }

        Map<String, Object> updateMap = new HashMap<>();
        if (updates.getName() != null) {
            updateMap.put("name", updates.getName());
        }
        if (updates.getLocation() != null) {
            updateMap.put("location", updates.getLocation());
        }
        if (updates.getStatus() != null) {
            updateMap.put("status", updates.getStatus());
        }
        if (updates.getDescription() != null) {
            updateMap.put("description", updates.getDescription());
        }
        if (updates.getPrice() != null) {
            updateMap.put("price", updates.getPrice());
        }
        if (updates.getHabCount() != null) {
            updateMap.put("habCount", updates.getHabCount());
        }
        if (updates.getLocalCount() != null) {
            updateMap.put("localCount", updates.getLocalCount());
        }
        if (updates.getLatitude() != null) {
            updateMap.put("latitude", updates.getLatitude());
        }
        if (updates.getLongitude() != null) {
            updateMap.put("longitude", updates.getLongitude());
        }
        if (updates.getRentMode() != null) {
            updateMap.put("rentMode", updates.getRentMode());
        }
        if (updates.getBuildingType() != null) {
            updateMap.put("buildingType", updates.getBuildingType());
        }
        if (updates.getAmenities() != null) {
            updateMap.put("amenities", updates.getAmenities());
        }
        if (updates.getPolicies() != null) {
            updateMap.put("policies", updates.getPolicies());
        }

        if (updates.getTenant() != null) {
            if (updates.getTenant().getName() == null || updates.getTenant().getName().trim().isEmpty()) {
                updateMap.put("tenant", null);
                updateMap.put("status", "disponible");
            } else {
                updateMap.put("tenant", updates.getTenant());
                updateMap.put("status", "rentada");
            }
        }

        if (!updateMap.isEmpty()) {
            ApiFuture<WriteResult> result = docRef.update(updateMap);
            result.get();
        }

        DocumentSnapshot updatedDoc = docRef.get().get();
        if (updatedDoc.exists()) {
            Property updatedProp = updatedDoc.toObject(Property.class);
            if (updatedProp != null) {
                updatedProp.setId(updatedDoc.getId());
            }
            return updatedProp;
        }

        return existing;
    }

    public boolean deleteProperty(String id, String userId) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection("properties").document(id);
        DocumentSnapshot document = docRef.get().get();

        if (!document.exists()) {
            return false;
        }

        Property existing = document.toObject(Property.class);
        if (userId != null && existing != null && existing.getUserId() != null
                && !existing.getUserId().equals(userId)) {
            throw new RuntimeException("No tienes permisos para eliminar esta propiedad.");
        }

        ApiFuture<WriteResult> result = docRef.delete();
        result.get();
        return true;
    }

    // ⬅️ AQUÍ SE AGREGARON 'rentMode' y 'buildingType' EN LOS PARÁMETROS Y EN EL FILTRADO
    public Map<String, Object> getPublicProperties(Double userLat, Double userLng, String type,
            String rentMode, String buildingType,
            Boolean petsAllowed, Boolean childrenAllowed,
            List<String> amenities, int page, int size) throws ExecutionException, InterruptedException {

        CollectionReference propertiesRef = db.collection("properties");

        // 1. Consulta base: Solo disponibles
        Query query = propertiesRef.whereEqualTo("status", "disponible");

        // 2. Filtro nativo por tipo de inmueble (VIVIENDA / EDIFICIO)
        if (type != null && !type.trim().isEmpty()) {
            query = query.whereEqualTo("type", type.toUpperCase());
        }

        // 3. Filtros nativos de políticas
        if (Boolean.TRUE.equals(petsAllowed)) {
            query = query.whereEqualTo("policies.petsAllowed", true);
        }
        if (Boolean.TRUE.equals(childrenAllowed)) {
            query = query.whereEqualTo("policies.childrenAllowed", true);
        }

        // Ejecutamos la consulta en Firestore
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        List<QueryDocumentSnapshot> documents = querySnapshot.get().getDocuments();

        List<Property> allProperties = new ArrayList<>();
        for (DocumentSnapshot doc : documents) {
            Property prop = doc.toObject(Property.class);
            if (prop != null) {
                prop.setId(doc.getId());
                allProperties.add(prop);
            }
        }

        // 4A. NUEVO: Filtrado por modalidad de renta (completo / individual)
        if (rentMode != null && !rentMode.trim().isEmpty()) {
            allProperties.removeIf(prop -> prop.getRentMode() == null || !prop.getRentMode().equalsIgnoreCase(rentMode));
        }

        // 4B. NUEVO: Filtrado por subtipo de edificio (normal / hibrido)
        if (buildingType != null && !buildingType.trim().isEmpty()) {
            allProperties.removeIf(prop -> prop.getBuildingType() == null || !prop.getBuildingType().equalsIgnoreCase(buildingType));
        }

        // 4C. Filtrado en memoria para Servicios (Agua, Internet, etc.)
        if (amenities != null && !amenities.isEmpty()) {
            allProperties.removeIf(prop -> prop.getAmenities() == null || !prop.getAmenities().containsAll(amenities));
        }

        // 5. Ordenamiento por distancia (Si hay coordenadas)
        if (userLat != null && userLng != null) {
            allProperties.sort((p1, p2) -> {
                if (p1.getLatitude() == null || p1.getLongitude() == null) {
                    return 1;
                }
                if (p2.getLatitude() == null || p2.getLongitude() == null) {
                    return -1;
                }

                double dist1 = calculateDistance(userLat, userLng, p1.getLatitude(), p1.getLongitude());
                double dist2 = calculateDistance(userLat, userLng, p2.getLatitude(), p2.getLongitude());

                return Double.compare(dist1, dist2);
            });
        }

        // 6. Paginación manual segura
        int totalElements = allProperties.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);
        if (totalPages == 0) {
            totalPages = 1;
        }

        int pageIndex = Math.max(1, page);
        int fromIndex = (pageIndex - 1) * size;
        int toIndex = Math.min(fromIndex + size, totalElements);

        List<Property> paginatedList = new ArrayList<>();
        if (fromIndex < totalElements) {
            paginatedList = allProperties.subList(fromIndex, toIndex);
        }

        // 7. Retorno al frontend
        Map<String, Object> response = new HashMap<>();
        response.put("content", paginatedList);
        response.put("totalPages", totalPages);
        response.put("totalElements", totalElements);

        return response;
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        int earthRadiusKm = 6371;

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.sin(dLon / 2) * Math.sin(dLon / 2) * Math.cos(lat1) * Math.cos(lat2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return earthRadiusKm * c;
    }
}