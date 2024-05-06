package pe.sanmiguel.bienestar.proyecto_gtics.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "sede_doctor")
public class SedeDoctor {

    @EmbeddedId
    private SedeDoctorId id;

    @MapsId("idDoctor")
    @ManyToOne
    @JoinColumn(name="idDoctor")
    private Doctor idDoctor;


    @MapsId("idSede")
    @ManyToOne
    @JoinColumn(name="idSede")
    private Sede idSede;

}
