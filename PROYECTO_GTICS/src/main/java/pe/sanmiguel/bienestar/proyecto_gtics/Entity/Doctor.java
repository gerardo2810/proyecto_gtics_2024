package pe.sanmiguel.bienestar.proyecto_gtics.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
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
    @NotBlank(message = "Debe ingresar un nombre")
    private String nombres;
    @Column
    @NotBlank(message = "Debe ingresar un apellido")
    private String apellidos;
    @Column
    @NotBlank(message = "Debe ingresar su dirección")
    private String direccion;
    @Column
    @Pattern(regexp="\\d+", message="El teléfono debe contener solo números")
    @Size(min=9 , message = "El celular debe tener al menos 9 dígitos")
    @NotBlank(message = "Debe ingresar un número de celular")
    @Positive
    private String celular;
    @Column

    @NotBlank(message = "Debe ingredar el distrito de residencia")
    private String distrito;
    @Column
    @NotBlank(message = "Debe ingresar la especialidad del doctor")
    private String especialidad;
    @Column
    @NotBlank(message = "Debe ingresar un correo")
    @Email(regexp = ".+@.+\\..+", message = "Debe ingresar un correo válido")
    private String correo;

    @Column
    @Pattern(regexp="\\d+", message="El DNI debe contener solo números")
    @Size(max=8, message = "Debe tener un máximo de 8 carácteres")
    @Size(min=8, message = "Debe tener un mínimo de 8 carácteres")
    @NotBlank(message = "Debe ingresar el número de su dni")
    @Positive
    private String dni;

}
