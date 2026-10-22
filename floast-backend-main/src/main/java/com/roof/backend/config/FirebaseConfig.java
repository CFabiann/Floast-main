package com.roof.backend.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

@Configuration
public class FirebaseConfig {

    @Bean
    public Firestore firestore() {
        try {
            InputStream serviceAccount;
            File renderSecret = new File("/etc/secrets/firebase-service-account.json");

            // 1. Preguntamos si el archivo secreto existe en la ruta de Render
            if (renderSecret.exists()) {
                serviceAccount = new FileInputStream(renderSecret);
                System.out.println("☁️ Entorno de NUBE detectado: Usando bóveda de Render.");
            } else {
                // 2. Si no existe, asumimos que estamos en el entorno local
                serviceAccount = new ClassPathResource("firebase-service-account.json").getInputStream();
                System.out.println("💻 Entorno LOCAL detectado: Usando carpeta resources.");
            }

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            // Iniciamos Firebase solo si no se ha iniciado antes
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                System.out.println("🔥 ¡ÉXITO! Firebase Admin SDK inicializado correctamente.");
            }

            // Devolvemos la instancia de Firestore ya autenticada para que Spring la inyecte en tus servicios
            return FirestoreClient.getFirestore(FirebaseApp.getInstance());

        } catch (Exception e) {
            System.err.println("❌ ERROR CRÍTICO AL INICIAR FIREBASE:");
            e.printStackTrace();
            throw new RuntimeException("Fallo al inicializar Firebase.", e);
        }
    }
}