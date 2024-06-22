package pe.sanmiguel.bienestar.proyecto_gtics;

import com.google.cloud.Timestamp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import java.util.concurrent.ExecutionException;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.json.JSONObject;

@Service
public class ChatHandler extends TextWebSocketHandler {


    @Autowired
    ChatService chatService;

    private static final Logger LOGGER = LoggerFactory.getLogger(ChatHandler.class);

    // Map to store chat sessions by chatId (chatId could be "userId1-userId2" or "userId2-userId1")
    private final ConcurrentHashMap<String, List<WebSocketSession>> chatSessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String uri = session.getUri().toString();
        String[] pathSegments = uri.split("/");
        if (pathSegments.length >= 5) {
            String userId1 = pathSegments[pathSegments.length - 2];
            String userId2 = pathSegments[pathSegments.length - 1];
            String chatId1 = userId1 + "-" + userId2;
            String chatId2 = userId2 + "-" + userId1;

            chatSessions.putIfAbsent(chatId1, new CopyOnWriteArrayList<>());
            chatSessions.putIfAbsent(chatId2, new CopyOnWriteArrayList<>());

            chatSessions.get(chatId1).add(session);
            chatSessions.get(chatId2).add(session);

            LOGGER.info("User connected: " + session.getId() + " to chat " + chatId1);
        } else {
            session.close(CloseStatus.BAD_DATA);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        chatSessions.values().forEach(sessions -> sessions.remove(session));
        LOGGER.info("User disconnected: " + session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String uri = session.getUri().toString();
        String[] pathSegments = uri.split("/");





        if (pathSegments.length >= 5) {
            String userId1 = pathSegments[pathSegments.length - 2];
            String userId2 = pathSegments[pathSegments.length - 1];
            String chatId1 = userId1 + "-" + userId2;
            String chatId2 = userId2 + "-" + userId1;

            List<WebSocketSession> sessions = chatSessions.get(chatId1);
            if (sessions == null) {
                sessions = chatSessions.get(chatId2);
            }


            if (sessions != null) {
                for (WebSocketSession webSocketSession : sessions) {
                    if (webSocketSession.isOpen()) {
                        webSocketSession.sendMessage(message);
                    }
                }
            }


            System.out.println( "ChatHandler --> userId1: "  + userId1 + " , userId2: " +  userId2 + " --> " +  message.getPayload());



            try {
                String payload = message.getPayload();
                JSONObject json = new JSONObject(payload);
                String actualMessage = json.getString("message");
                String rol = json.getString("userRol");
                String timestamp = json.getString("timestamp");
                Integer idChat  = json.getInt("idChat");


                ChatFirebase chat = new ChatFirebase();
                chat.setIdChat(idChat);
                chat.setIdRol(rol);
                chat.setMensaje(actualMessage);
                chat.setTimeDate(Timestamp.parseTimestamp(timestamp));

                chatService.addChat(chat);


            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
                System.out.println("Error adding chat: " + e.getMessage());
            }

        }
    }
}