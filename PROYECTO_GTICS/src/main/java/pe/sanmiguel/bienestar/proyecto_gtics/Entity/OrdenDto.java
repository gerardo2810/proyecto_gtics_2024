package pe.sanmiguel.bienestar.proyecto_gtics.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
public class OrdenDto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "Debe seleccionar un doctor")
    private String idDoctor;

    @NotBlank(message = "Debe seleccionar la hora de entrega")
    private String horaEntrega;

    @NotBlank(message = "Debe seleccionar al menos un medicamento")
    private String listaIds;
    
    @NotEmpty(message = "Debe seleccionar una imagen")
    @Lob
    private byte[] imagen;

    // Constructor, getters y setters

    public void setImagen(MultipartFile file) {
        try {
            this.imagen = file.getBytes();
        } catch (IOException e) {
            throw new RuntimeException("Error al convertir la imagen a bytes", e);
        }
    }



}
