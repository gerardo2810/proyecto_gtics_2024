package pe.sanmiguel.bienestar.proyecto_gtics;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class ChatService {

    @Autowired
    private Firestore firestore;

    public List<ChatFirebase> getChatsByRole(String role) throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = firestore.collection("chats")
                .whereEqualTo("idRol", role)
                .get();

        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        List<ChatFirebase> chats = new ArrayList<>();

        for (QueryDocumentSnapshot document : documents) {
            ChatFirebase chat = document.toObject(ChatFirebase.class);
            chats.add(chat);
        }

        return chats;
    }

    public List<ChatFirebase> getChatsByIdChat(Integer idChat) throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = firestore.collection("chats")
                .whereEqualTo("idChat", idChat)
                .get();

        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        List<ChatFirebase> chats = new ArrayList<>();

        for (QueryDocumentSnapshot document : documents) {
            ChatFirebase chat = document.toObject(ChatFirebase.class);
            chats.add(chat);
        }

        return chats;
    }



}
