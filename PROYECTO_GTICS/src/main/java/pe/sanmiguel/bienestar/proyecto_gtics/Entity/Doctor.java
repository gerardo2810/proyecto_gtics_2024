package pe.sanmiguel.bienestar.proyecto_gtics.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "doctor")
public class Doctor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Integer idDoctor;

    @Column
    @NotBlank(message = " ")
    private String nombres;
    @Column
    @NotBlank(message = " ")
    private String apellidos;
    @Column
    @NotBlank(message = " ")
    private String direccion;
    @Column
    @Pattern(regexp="\\d+", message="El teléfono debe contener solo números")
    @NotBlank(message = " ")
    private String celular;
    @Column
    @NotBlank(message = " ")
    private String distrito;
    @Column
    @NotBlank(message = " ")
    private String especialidad;
    @Column
    @NotBlank(message = " ")
    private String correo;
    @Column
    @Pattern(regexp="\\d+", message="El DNI debe contener solo números")
    @Size(max=8, message = "Debe tener un máximo de 8 carácteres")
    @NotBlank(message = " ")
    private String dni;

}
