package pe.sanmiguel.bienestar.proyecto_gtics.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "orden_contenido")
public class OrdenContenido {

    @EmbeddedId
    private OrdenContenidoId id;

    @MapsId("idOrden")
    @ManyToOne
    @JoinColumn(name="idOrden")
    private Orden idOrden;


    @MapsId("idMedicamento")
    @ManyToOne
    @JoinColumn(name="idMedicamento")
    private Medicamento idMedicamento;


    @Column
    private int cantidad;

}
