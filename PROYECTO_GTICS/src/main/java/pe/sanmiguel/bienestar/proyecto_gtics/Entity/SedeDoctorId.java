package pe.sanmiguel.bienestar.proyecto_gtics.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@Embeddable
public class SedeDoctorId implements Serializable {
    @Column(name = "idDoctor")
    private Integer idDoctor;

    @Column(name = "idSede")
    private Integer idSede;
}
