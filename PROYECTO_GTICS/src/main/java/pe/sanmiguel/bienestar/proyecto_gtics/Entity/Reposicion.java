package pe.sanmiguel.bienestar.proyecto_gtics.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@Entity
@Table(name= "reposicion")
public class Reposicion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Integer idReposicion;
    @ManyToOne
    @JoinColumn(name="idEstado")
    private EstadoReposicion estado;
    @ManyToOne
    @JoinColumn(name="idSede")
    private Sede idSede;
    @Column
    private String tracking;
    @Column
    private Date fechaIni;
    @Column
    private Date fechaFin;
    @Column
    private boolean pagado;
    @Column
    private float precioTotal;
}
