package pe.sanmiguel.bienestar.proyecto_gtics.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import pe.sanmiguel.bienestar.proyecto_gtics.ValidationGroup.OptionalValidationsGroup;

@Getter
@Setter
@Entity
@Table(name = "usuario")
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Integer idUsuario = 0;

    @Column(name = "idRol")
    private Integer rol;

    @Column
    @NotBlank(message = "Debe ingresar un correo")
    @Email(regexp = ".+@.+\\..+", message = "Debe ingresar un correo válido")
    private String correo;

    @Column
    @NotBlank(message = "Debe ingresar una contraseña", groups = OptionalValidationsGroup.class)
    @Size(min = 8, message = "Debe ingresar al menos 8 caracteres", groups = OptionalValidationsGroup.class)
    private String contrasena;

    @Column
    @NotBlank(message = "Debe ingresar un nombre")
    @Size(max = 45, message = "No debe exceder de 45 caracteres")
    private String nombres;

    @Column
    @NotBlank(message = "Debe ingresar un apellido")
    @Size(max = 45, message = "No debe exceder de 45 caracteres")
    private String apellidos;

    @Column
    @Pattern(regexp="\\d+", message="El teléfono debe contener solo números")
    @Size(min=9 , message = "El celular debe tener al menos 9 dígitos")
    @NotBlank(message = "Debe ingresar un número de celular")
    @Positive
    private String celular;

    @Column
    @Pattern(regexp="\\d+", message="El DNI debe contener solo números")
    @Size(max=8, message = "Debe tener un máximo de 8 carácteres")
    @Size(min=8, message = "Debe tener un mínimo de 8 carácteres")
    @NotBlank(message = "Debe ingresar el número de su dni")
    @Positive
    private String dni;

    @Column
    @NotBlank(message = "Debe ingresar su dirección")
    @Size(max = 60, message = "Debe contener como máximo 60 caracteres")
    private String direccion;

    @Column
    @NotBlank(message = "Debe seleccionar el distrito de residencia")
    @Size(max = 30, message = "No debe exceder de 30 caracteres")
    private String distrito;

    @Column
    @NotBlank(message = "Debe ingresar su seguro")
    @Size(max = 30, message = "No debe exceder de 30 caracteres")
    private String seguro;

    @Column(name = "estado_usuario")
    private Integer estadoUsuario;

}
