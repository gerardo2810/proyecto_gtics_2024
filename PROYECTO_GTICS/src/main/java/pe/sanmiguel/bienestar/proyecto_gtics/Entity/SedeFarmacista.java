package pe.sanmiguel.bienestar.proyecto_gtics.Entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "sede_farmacista")
public class SedeFarmacista {

    @EmbeddedId
    private SedeFarmacistaId id;

    @MapsId("idSede") //nombre del atributo clase compuesta
    @ManyToOne
    @JoinColumn(name="idSede") //nombre del campo de la tabla sede_farmacista
    private Sede idSede; //nombre del atributo del id de la clase que se relaciona con esa columna


    @MapsId("idFarmacista")
    @ManyToOne
    @JoinColumn(name="idFarmacista")
    private Usuario idFarmacista;

    @Column(name = "codigoMed")
    private String codigoMed;

    @Column(name = "aprobado")
    private Integer aprobado;










}
