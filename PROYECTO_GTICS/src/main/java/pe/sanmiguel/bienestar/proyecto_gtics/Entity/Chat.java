package pe.sanmiguel.bienestar.proyecto_gtics.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "chat")
public class Chat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Integer idChat;

    @ManyToOne
    @JoinColumn(name="idUser1")
    private Usuario idUser1;

    @ManyToOne
    @JoinColumn(name="idUser2")
    private Usuario idUser2;

    @ManyToOne
    @JoinColumn(name="idOrden")
    private Orden orden;


}
