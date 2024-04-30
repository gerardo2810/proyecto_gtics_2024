package pe.sanmiguel.bienestar.proyecto_gtics.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@Embeddable
public class ReposicionContenidoId implements Serializable {
    @Column(name = "idMedicamento")
    private Integer idMedicamento;

    @Column(name = "idReposicion")
    private Integer idReposicion;


}
