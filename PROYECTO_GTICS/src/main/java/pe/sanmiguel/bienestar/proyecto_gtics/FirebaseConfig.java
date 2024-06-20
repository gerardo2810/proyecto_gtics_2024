package pe.sanmiguel.bienestar.proyecto_gtics;
import java.io.FileInputStream;

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

        FileInputStream serviceAccount = (FileInputStream) firebaseJsonRepository.firebaseJSON();

        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount)).build();

        FirebaseApp firebaseApp = FirebaseApp.initializeApp(options);

        return FirestoreClient.getFirestore(firebaseApp);

    }

}