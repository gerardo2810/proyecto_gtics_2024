package pe.sanmiguel.bienestar.proyecto_gtics.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name= "sede")
public class Sede {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter @Setter @Column(name="id")
    private int idSede;
    @Getter @Setter @Column
    private String direccion;
    @Getter @Setter @Column
    private String nombre;

    @ManyToOne
    @Getter @Setter @JoinColumn(name="idAdmin")
    private Usuario admin;
}
