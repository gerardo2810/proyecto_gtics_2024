package pe.sanmiguel.bienestar.proyecto_gtics.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
public class OrdenDto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull(message = "Debe seleccionar un doctor")
    private Integer idDoctor;

    @Pattern(regexp = "^([01]?[0-9]|2[0-3]):[0-5][0-9]$", message = "Formato de hora incorrecto")
    @NotBlank(message = "Debe seleccionar la hora de entrega")
    private String horaEntrega;

    @NotBlank(message = "Debe seleccionar al menos un medicamento")
    private String listaIds;

    @NotBlank(message = "Debe seleccionar al menos un medicamento")
    private String priceTotal;

    @Min(value = 1, message = "Debe seleccionar una sede")
    @Max(value = 5, message = "Debe seleccionar una sede")
    @NotNull(message = "Debe seleccionar una sede")
    private Integer sedeId;

    @NotEmpty(message = "Debe seleccionar la imagen de su receta")
    @Lob
    private byte[] file;


    public void setFile(MultipartFile file) {
        try {
            this.file = file.getBytes();
        } catch (IOException e) {
            throw new RuntimeException("Error al convertir la imagen a bytes", e);
        }
    }



}
