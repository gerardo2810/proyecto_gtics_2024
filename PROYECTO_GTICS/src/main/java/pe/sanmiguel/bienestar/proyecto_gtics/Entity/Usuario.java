package pe.sanmiguel.bienestar.proyecto_gtics.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import pe.sanmiguel.bienestar.proyecto_gtics.ValidationGroup.FarmacistaValidationsGroup;
import pe.sanmiguel.bienestar.proyecto_gtics.ValidationGroup.OptionalValidationsGroup;
import pe.sanmiguel.bienestar.proyecto_gtics.ValidationGroup.RegisterValidationsGroup;

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
    @NotBlank(message = "Debe ingresar un correo", groups = {RegisterValidationsGroup.class, FarmacistaValidationsGroup.class, OptionalValidationsGroup.class})
    @Email(regexp = ".+@.+\\..+", message = "Debe ingresar un correo válido", groups = {RegisterValidationsGroup.class, FarmacistaValidationsGroup.class, OptionalValidationsGroup.class})
    private String correo;

    @Column
    @NotBlank(message = "Debe ingresar una contraseña", groups = {RegisterValidationsGroup.class, OptionalValidationsGroup.class})
    @Size(min = 8, message = "Debe ingresar al menos 8 caracteres", groups = {RegisterValidationsGroup.class, OptionalValidationsGroup.class})
    private String contrasena;

    @Column
    @NotBlank(message = "Debe ingresar un nombre", groups = {RegisterValidationsGroup.class, FarmacistaValidationsGroup.class})
    @Size(max = 45, message = "No debe exceder de 45 caracteres", groups = {RegisterValidationsGroup.class, FarmacistaValidationsGroup.class})
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚüÜñÑ\\s]+$", message = "El nombre solo puede contener letras y espacios", groups = {RegisterValidationsGroup.class, FarmacistaValidationsGroup.class})
    @NotBlank(message = "Debe ingresar un nombre")
    @Size(max = 45, message = "No debe exceder de 45 caracteres")
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚüÜñÑ\\s]+$", message = "El nombre solo puede contener letras y espacios")
    private String nombres;

    @Column
    @NotBlank(message = "Debe ingresar un apellido", groups = {RegisterValidationsGroup.class, FarmacistaValidationsGroup.class})
    @Size(max = 45, message = "No debe exceder de 45 caracteres", groups = {RegisterValidationsGroup.class, FarmacistaValidationsGroup.class})
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚüÜñÑ\\s]+$", message = "El nombre solo puede contener letras y espacios", groups = {RegisterValidationsGroup.class, FarmacistaValidationsGroup.class})
    @NotBlank(message = "Debe ingresar un apellido")
    @Size(max = 45, message = "No debe exceder de 45 caracteres")
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚüÜñÑ\\s]+$", message = "El nombre solo puede contener letras y espacios")
    private String apellidos;

    

    @Column
    @Pattern(regexp="\\d+", message="El teléfono debe contener solo números", groups = {RegisterValidationsGroup.class, FarmacistaValidationsGroup.class})
    @Size(min=9 , message = "El celular debe tener al menos 9 dígitos", groups = {RegisterValidationsGroup.class, FarmacistaValidationsGroup.class})
    @NotBlank(message = "Debe ingresar un número de celular", groups = {RegisterValidationsGroup.class, FarmacistaValidationsGroup.class})
    @Positive(groups = {RegisterValidationsGroup.class, FarmacistaValidationsGroup.class})
    @Pattern(regexp="\\d+", message="El teléfono debe contener solo números")
    @Size(min=9, max = 9, message = "El celular debe tener al menos 9 dígitos")
    @NotBlank(message = "Debe ingresar un número de celular")
    @Positive(message = "El número debe ser mayor a cero")
    private String celular;

    @Column
    @Pattern(regexp="\\d+", message="El DNI debe contener solo números", groups = {RegisterValidationsGroup.class, FarmacistaValidationsGroup.class})
    @Size(min = 8, max = 8, message = "Debe tener 8 dígitos", groups = {RegisterValidationsGroup.class, FarmacistaValidationsGroup.class})
    @NotBlank(message = "Debe ingresar el número de su dni", groups = {RegisterValidationsGroup.class, FarmacistaValidationsGroup.class})
    @Positive(message= "Deben ser un número positivo", groups = {RegisterValidationsGroup.class, FarmacistaValidationsGroup.class})
    @Pattern(regexp="\\d+", message="El DNI debe contener solo números")
    @Size(min = 8, max = 8, message = "Debe tener 8 dígitos")
    @NotBlank(message = "Debe ingresar el número de su dni")
    @Positive(message= "Deben ser un número positivo")
    private String dni;

    @Column
    @NotBlank(message = "Debe ingresar su dirección", groups = {RegisterValidationsGroup.class, FarmacistaValidationsGroup.class})
    @Size(max = 60, message = "Debe contener como máximo 60 caracteres", groups = {RegisterValidationsGroup.class, FarmacistaValidationsGroup.class})
    @NotBlank(message = "Debe ingresar su dirección")
    @Size(max = 60, message = "Debe contener como máximo 60 caracteres")
    private String direccion;

    @Column
    @Size(max = 30, message = "No debe exceder de 30 caracteres", groups = {RegisterValidationsGroup.class, FarmacistaValidationsGroup.class})
    @NotBlank(message = "Debe colocar su distrito de residencia", groups = {RegisterValidationsGroup.class, FarmacistaValidationsGroup.class})
    @Size(max = 30, message = "No debe exceder de 30 caracteres")
    @NotBlank(message = "Debe colocar su distrito de residencia")
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚüÜñÑ\\s]+$", message = "El nombre solo puede contener letras y espacios")
    private String distrito;

    @Column
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚüÜñÑ\\s]+$", message = "No debe incluir números", groups = {RegisterValidationsGroup.class, FarmacistaValidationsGroup.class})
    @NotBlank(message = "Debe ingresar su seguro", groups = {RegisterValidationsGroup.class, FarmacistaValidationsGroup.class})
    @Size(max = 30, message = "No debe exceder de 30 caracteres", groups = {RegisterValidationsGroup.class, FarmacistaValidationsGroup.class})
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚüÜñÑ\\s]+$", message = "No debe incluir números")
    @NotBlank(message = "Debe ingresar su seguro")
    @Size(max = 30, message = "No debe exceder de 30 caracteres")
    private String seguro;

    @Column(name = "estado_usuario")
    private Integer estadoUsuario;


}
