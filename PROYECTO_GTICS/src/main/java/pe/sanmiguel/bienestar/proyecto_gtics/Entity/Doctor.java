package pe.sanmiguel.bienestar.proyecto_gtics.Entity;

import jakarta.persistence.*;
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
    private String nombres;
    @Column
    private String apellidos;
    @Column
    private String direccion;
    @Column
    private String celular;
    @Column
    private String distrito;
    @Column
    private String especialidad;
    @Column
    private String correo;
    @Column
    private String dni;

}
