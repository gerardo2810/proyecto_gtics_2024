package pe.sanmiguel.bienestar.proyecto_gtics.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "medicamento")
public class Medicamento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idMedicamento;
    @Column
    private String nombre;
    @Column
    private String unidad;
    @Column
    private String descripcion;
    @Column
    private String categorias;
    @Column
    private String componente;
    @Column
    private String precioCompra;
    @Column
    private Float precioVenta;
    @Column
    private String recetable;
    @Column
    @Lob
    private byte[] imagen;
}
