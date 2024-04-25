package pe.sanmiguel.bienestar.proyecto_gtics.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@Embeddable
public class SedeFarmacistaId implements Serializable {

    @Column(name = "idSede")
    private Integer idSede;

    @Column(name = "idFarmacista")
    private Integer idFarmacista;




}
