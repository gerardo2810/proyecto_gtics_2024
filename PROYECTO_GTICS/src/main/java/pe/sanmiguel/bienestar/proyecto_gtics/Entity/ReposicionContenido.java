package pe.sanmiguel.bienestar.proyecto_gtics.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "reposicion_contenido")
public class ReposicionContenido {
    @EmbeddedId
    private ReposicionContenidoId id;

    @MapsId("idReposicion")
    @ManyToOne
    @JoinColumn(name="idReposicion")
    private Reposicion idReposicion;

    @MapsId("idMedicamento")
    @ManyToOne
    @JoinColumn(name="idMedicamento")
    private Medicamento idMedicamento;

    @Column
    private int cantidad;
}
