package pe.sanmiguel.bienestar.proyecto_gtics.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "tokens")
public class Token {
    @Id
    @Column(name = "idtokens")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idtokens;

    @Column(name = "valor")
    private String valor;

    @Column(name = "nombre")
    private String nombre;
}
