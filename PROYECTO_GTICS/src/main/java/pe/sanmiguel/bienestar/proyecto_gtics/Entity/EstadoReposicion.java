package pe.sanmiguel.bienestar.proyecto_gtics.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name= "estado_reposicion")
public class EstadoReposicion {
    @Id
    @Column(name="id")
    private Integer idEstado;
    @Column
    private String nombre;
}
