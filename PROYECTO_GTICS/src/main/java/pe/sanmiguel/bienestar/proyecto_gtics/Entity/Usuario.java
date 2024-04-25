package pe.sanmiguel.bienestar.proyecto_gtics.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
@Entity
@Table(name = "usuario")
public class Usuario {
    @Id
    @Column(name="id")
    private Integer idUsuario;
    @Column(name = "idRol")
    private Integer rol;
    @Column
    private String correo;
    @Column
    private String contrasena;
    @Column
    private String nombres;
    @Column
    private String apellidos;
    @Column
    private String celular;
    @Column
    private String dni;
    @Column
    private String direccion;
    @Column
    private String distrito;
    @Column
    private String seguro;
    @Column(name = "estado_usuario")
    private Integer estadoUsuario;

}
