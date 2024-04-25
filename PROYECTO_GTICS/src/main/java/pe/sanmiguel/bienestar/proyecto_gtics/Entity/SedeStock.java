package pe.sanmiguel.bienestar.proyecto_gtics.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "sede_stock")
@Getter
@Setter
public class SedeStock {

    @EmbeddedId
    private SedeStockId id;

    @MapsId("idSede") //atributo de la clase de la llave compuesta
    @ManyToOne
    @JoinColumn(name = "idSede") //nombre de la columna de la tabla SedeStockId
    private Sede idSede;

    @MapsId("idMedicamento")
    @ManyToOne
    @JoinColumn(name = "idMedicamento")
    private Medicamento idMedicamento;

    @Column(name = "cantidad")
    private int cantidad;



}
