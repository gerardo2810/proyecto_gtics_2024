package pe.sanmiguel.bienestar.proyecto_gtics.Entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Getter
@Setter
@Entity
public class ValidateImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;


    @NotEmpty(message = "Debe seleccionar la imagen del medicamento")
    @Lob
    @Size(max = 1048576, message = "El tamaño máximo de la imagen debe ser de 1 MB")
    private byte[] file;

    public void setFile(MultipartFile file) {
        try {
            this.file = file.getBytes();
        } catch (IOException e) {
            throw new RuntimeException("Error al convertir la imagen a bytes", e);
        }
    }


}
