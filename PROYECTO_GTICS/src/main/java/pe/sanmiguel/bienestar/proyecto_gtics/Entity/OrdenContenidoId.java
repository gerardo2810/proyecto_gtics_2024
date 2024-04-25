package pe.sanmiguel.bienestar.proyecto_gtics.Entity;


import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@Embeddable
public class OrdenContenidoId implements Serializable {

    @Column(name = "idOrden")
    private Integer idOrden;

    @Column(name = "idMedicamento")
    private Integer idMedicamento;

}
