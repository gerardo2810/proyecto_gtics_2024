package pe.sanmiguel.bienestar.proyecto_gtics.Entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import pe.sanmiguel.bienestar.proyecto_gtics.ValidationGroup.FarmacistaValidationsGroup;
import pe.sanmiguel.bienestar.proyecto_gtics.ValidationGroup.RegisterValidationsGroup;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
@Entity
public class Tarjeta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Pattern(regexp="\\d+", message="Debe contener solo números")
    @Size(min=16, max = 16, message = "Debe tener 16 dígitos")
    @NotBlank(message = "Debe ingresar su numero de tarjeta")
    @Positive(message = "El número debe ser mayor a cero")
    private String numero_tarjeta;

    @NotBlank(message = "Debe seleccionar el mes")
    private String mes;

    @NotBlank(message = "Debe seleccionar el año")
    private String anio;

    @NotBlank(message = "Debe colocar el código CVV")
    @Size(min=3, max = 3, message = "Debe tener 3 dígitos")
    @Pattern(regexp="\\d+", message="Debe contener solo números")
    private String cvv;

    @NotBlank(message = "Debe colocar el nombre del titular de la tarjeta")
    @Pattern(regexp = "^[A-Za-zÁÉÍÓÚáéíóúñÑ ]+$", message = "El nombre del titular solo puede contener letras")
    private String titular;

}
