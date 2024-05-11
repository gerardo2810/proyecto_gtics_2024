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
    @NotBlank(message = "Debe ingresar su nombre")
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚüÜñÑ\\s]+$", message = "El nombre solo puede contener letras y espacios")
    private String nombres;

    @Column
    @NotBlank(message = "Debe ingresar su apellido")
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚüÜñÑ\\s]+$", message = "El nombre solo puede contener letras y espacios")
    private String apellidos;

    @Column
    @Pattern(regexp="\\d+", message="El teléfono debe contener solo números")
    @Size(min=9 , message = "El celular debe tener al menos 9 dígitos")
    @NotBlank(message = "Debe ingresar un número de celular")
    @Positive
    private String celular;

    @Column
    @Pattern(regexp="\\d+", message="El DNI debe contener solo números")
    @Size(min = 8, max = 8, message = "Debe tener 8 dígitos")
    @NotBlank(message = "Debe ingresar el número de su dni")
    @Positive(message= "Deben ser un número positivo")
    private String dni;

    @Column
    @NotBlank(message = "Debe ingresar su dirección")
    private String direccion;

    @Column
    @NotBlank(message = "Debe colocar su distrito de residencia")
    private String distrito;

    @Column
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚüÜñÑ\\s]+$", message = "No debe incluir números")
    @NotBlank(message = "Debe ingresar su seguro")
    private String seguro;

    @Column(name = "estado_usuario")
    private Integer estadoUsuario;

}
