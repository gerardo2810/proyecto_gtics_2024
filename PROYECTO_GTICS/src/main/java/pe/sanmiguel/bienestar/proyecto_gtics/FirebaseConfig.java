package pe.sanmiguel.bienestar.proyecto_gtics;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import pe.sanmiguel.bienestar.proyecto_gtics.Repository.ChatRepository;


@Configuration
public class FirebaseConfig {

    @Autowired
    private ChatRepository firebaseJsonRepository;
    @Bean
    public Firestore firestore() throws Exception {

        String json = firebaseJsonRepository.firebaseJSON();

        InputStream serviceAccount = new ByteArrayInputStream(json.getBytes());

        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount)).build();

        FirebaseApp firebaseApp;

        // Verifica que no se genere una instancia duplicada de Firebase
        try {
            firebaseApp = FirebaseApp.getInstance();
        } catch (IllegalStateException e) {
            firebaseApp = FirebaseApp.initializeApp(options);
        }

        return FirestoreClient.getFirestore(firebaseApp);

    }
}