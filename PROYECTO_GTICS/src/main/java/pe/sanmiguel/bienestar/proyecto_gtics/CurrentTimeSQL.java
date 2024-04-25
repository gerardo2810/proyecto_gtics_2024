package pe.sanmiguel.bienestar.proyecto_gtics;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class CurrentTimeSQL {

    public static LocalDate getCurrentDate() {
        LocalDateTime now = LocalDateTime.now();
        return now.toLocalDate();
    }
}
