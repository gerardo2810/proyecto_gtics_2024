package pe.sanmiguel.bienestar.proyecto_gtics.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "estado_usuario")
public class EstadoUsuario {
    @Id
    @Column(name="id")
    private Integer idEstadoUsuario;
    @Column
    private String nombre;

    // Relación bidireccional con Usuario
    @OneToMany(mappedBy = "estadoUsuario")
    private Set<Usuario> usuarios;
}
