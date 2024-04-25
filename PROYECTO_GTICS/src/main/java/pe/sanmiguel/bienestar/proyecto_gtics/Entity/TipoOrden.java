package pe.sanmiguel.bienestar.proyecto_gtics.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "tipo_orden")
public class TipoOrden {
    @Id
    @Column(name="id")
    private Integer idTipoOrden;
    @Column
    private String nombre;
}
