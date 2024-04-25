package pe.sanmiguel.bienestar.proyecto_gtics.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "estado_preorden")
public class EstadoPreOrden {
    @Id
    @Column(name="id")
    private Integer idEstadoPreOrden;
    @Column
    private String nombre;
}
