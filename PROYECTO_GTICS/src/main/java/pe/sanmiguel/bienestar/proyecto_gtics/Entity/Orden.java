package pe.sanmiguel.bienestar.proyecto_gtics.Entity;

import com.mysql.cj.jdbc.Blob;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "orden")
public class Orden {
    @Id
    @Column(name="id")
    private Integer idOrden;
    @Column
    private String tracking;
    @Column
    private LocalDate fechaIni;
    @Column
    private LocalDate fechaFin;
    @Column
    private float precioTotal;
    @Column
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
    @Column(name = "estado_preorden")
    private Integer estadoPreOrden;
    @Column(name="imagenReceta")
    @Lob
    private byte[] imagen;


}
