package pe.sanmiguel.bienestar.proyecto_gtics;
import com.google.cloud.Timestamp;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class TimeStamp{

    public static String formatTimestamp(Timestamp timestamp) {
        Instant instant = timestamp.toDate().toInstant();
        LocalDateTime dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:mm a");
        String formattedDate = dateFormatter.format(dateTime);
        String formattedTime = timeFormatter.format(dateTime);

        return formattedDate + " " + formattedTime;
    }
}
