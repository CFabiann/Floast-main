package com.roof.backend.services;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.roof.backend.models.Unit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
public class UnitService {

    @Autowired
    private Firestore db;

    public Unit updateUnit(String propId, String unitId, Unit updates) throws ExecutionException, InterruptedException {
        // Entramos a la propiedad y luego a la subcolección de "unidades"
        DocumentReference docRef = db.collection("properties").document(propId)
                                     .collection("units").document(unitId);

        Map<String, Object> updateMap = new HashMap<>();
        if (updates.getLabel() != null) updateMap.put("label", updates.getLabel());
        if (updates.getStatus() != null) updateMap.put("status", updates.getStatus());
        if (updates.getPrice() != null) updateMap.put("price", updates.getPrice());
        if (updates.getArea() != null) updateMap.put("area", updates.getArea());
        if (updates.getDescription() != null) updateMap.put("description", updates.getDescription());
        if (updates.getNotes() != null) updateMap.put("notes", updates.getNotes());

        // LÓGICA DEL INQUILINO 
        if (updates.getTenant() != null) {
            if (updates.getTenant().getName() == null || updates.getTenant().getName().trim().isEmpty()) {
                updateMap.put("tenant", null);
                updateMap.put("status", "disponible"); // ⬅️ NUEVO: Si borramos inquilino, se libera el cuarto
            } else {
                updateMap.put("tenant", updates.getTenant());
                updateMap.put("status", "rentada");  
            }
        }

        if (!updateMap.isEmpty()) {
            // Usamos merge() para que Firebase no arroje error si la unidad es nueva
            ApiFuture<WriteResult> result = docRef.set(updateMap, SetOptions.merge());
            result.get();
        }

        return updates;
    }
    
    public List<Unit> getUnitsByPropertyId(String propId) throws ExecutionException, InterruptedException {
        CollectionReference unitsRef = db.collection("properties").document(propId).collection("units");
        
        ApiFuture<QuerySnapshot> future = unitsRef.get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();

        List<Unit> units = new java.util.ArrayList<>();
        for (DocumentSnapshot doc : documents) {
            Unit unit = doc.toObject(Unit.class);
            if (unit != null) {
                unit.setId(doc.getId()); // Aseguramos el ID
                units.add(unit);
            }
        }
        return units;
    }
}