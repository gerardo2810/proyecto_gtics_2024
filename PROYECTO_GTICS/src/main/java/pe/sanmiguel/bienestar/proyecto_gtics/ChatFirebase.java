package pe.sanmiguel.bienestar.proyecto_gtics;


import lombok.Getter;
import lombok.Setter;

import com.google.cloud.Timestamp;

@Getter
@Setter
public class ChatFirebase {

    public Integer idChat;
    public String idRol;
    public String mensaje;
    public Timestamp timeDate;

    @Override
    public String toString() {
        return "ChatFirebase{" +
                "idChat=" + idChat +
                ", idRol='" + idRol + '\'' +
                ", mensaje='" + mensaje + '\'' +
                ", timeDate='" + timeDate + '\'' +
                '}';
    }

    public String getFormattedDate() {
        return TimeStamp.formatTimestamp(this.timeDate).split(" ")[0];
    }

    public String getFormattedTime() {
        return TimeStamp.formatTimestamp(this.timeDate).split(" ")[1] + " " + TimeStamp.formatTimestamp(this.timeDate).split(" ")[2];
    }

}
