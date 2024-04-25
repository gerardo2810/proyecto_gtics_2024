package pe.sanmiguel.bienestar.proyecto_gtics.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name= "estado_orden")
public class EstadoOrden {
    @Id
    @Column(name="id")
    private Integer idOrden;
    @Column
    private String nombre;
}
