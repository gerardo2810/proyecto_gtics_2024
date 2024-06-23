package pe.sanmiguel.bienestar.proyecto_gtics;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class ChatService {

    @Autowired
    private Firestore firestore;


    public String addChat(ChatFirebase chat) throws ExecutionException, InterruptedException {
        CollectionReference chats = firestore.collection("chats");
        ApiFuture<DocumentReference> result = chats.add(chat);
        return result.get().getId();
    }



    public List<ChatFirebase> getChatsByIdChat(Integer idChat) throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = firestore.collection("chats")
                .whereEqualTo("idChat", idChat)
                .orderBy("timeDate", Query.Direction.ASCENDING)  // Ordenar por campo timeDate
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
