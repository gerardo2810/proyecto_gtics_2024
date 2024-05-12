package pe.sanmiguel.bienestar.proyecto_gtics.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "medicamento")
public class Medicamento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idMedicamento;
    @Column
    @NotBlank(message = "Debe ingresar un nombre")
    private String nombre;
    @Column
    @Pattern(regexp="\\d+", message="La unidad debe contener solo número")
    @Positive(message = "Debe ser positivo")
    @NotBlank(message = "Debe ingresar una unidad")
    private String unidad;
    @Column
    @NotBlank(message = "Debe ingresar una descripción")
    private String descripcion;
    @Column
    private String categorias;
    @Column
    @NotBlank(message = "Ingrese por lo menos un componente")
    private String componente;
    @Column
    @DecimalMin(value = "0.00", inclusive = false, message = "El precio de compra debe ser mayor que 0")
    @DecimalMax(value = "9999.99", message = "El precio de compra debe tener como máximo dos decimales")
    @NotNull(message = "Ingrese el precio de venta")
    private BigDecimal precioCompra;
    @Column
    @DecimalMin(value = "0.00", inclusive = false, message = "El precio de venta debe ser mayor que 0")
    @DecimalMax(value = "9999.99", message = "El precio de compra debe tener como máximo dos decimales")
    @NotNull(message = "Ingrese el precio de compra")
    private BigDecimal precioVenta;
    @Column
    private String recetable;

    @Column(name = "estado")
    private Integer estado;

    @Column
    private String imagen;
}
