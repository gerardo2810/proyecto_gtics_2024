package pe.sanmiguel.bienestar.proyecto_gtics.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "orden")
public class Orden {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Integer idOrden;
    @Column(name="tracking")
    private String tracking;
    @Column(name="imagenReceta")
    @Lob
    private byte[] imagen;
    @Column(name = "fechaIni")
    private LocalDateTime fechaIni;
    @Column(name = "fechaFin")
    private LocalDateTime fechaFin;
    @Column(name = "precioTotal")
    private float precioTotal;
    @Column(name = "idFarmacista")
    private Integer idFarmacista;
    @ManyToOne
    @JoinColumn(name="idPaciente")
    private Usuario paciente;
    @Column(name="idTipo")
    private Integer tipoOrden;
    @Column(name="idEstado")
    private Integer estadoOrden;
    @ManyToOne
    @JoinColumn(name="idSede")
    private Sede sede;
    @ManyToOne
    @JoinColumn(name = "idDoctor")
    private Doctor doctor;
    @Column(name="idOrden")
    private Integer ordenParent;
    @Column(name = "estadoPreorden")
    private Integer estadoPreOrden;
    @Column(name = "seguroUsado")
    private String seguroUsado;

}
