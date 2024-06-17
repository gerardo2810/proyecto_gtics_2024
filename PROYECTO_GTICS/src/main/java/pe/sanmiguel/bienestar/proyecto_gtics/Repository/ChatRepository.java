package pe.sanmiguel.bienestar.proyecto_gtics.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pe.sanmiguel.bienestar.proyecto_gtics.Entity.Chat;
import pe.sanmiguel.bienestar.proyecto_gtics.Entity.Doctor;

import java.util.List;

public interface ChatRepository extends JpaRepository<Chat, Integer> {


    @Query(nativeQuery = true, value = "SELECT * FROM proyecto_gtics.chat where idUser1=?;")
    List<Chat> listaChatsParaPaciente(int idPaciente);

    @Query(nativeQuery = true, value = "SELECT * FROM proyecto_gtics.chat where idUser2=?;")
    List<Chat> listaChatsParaFarmacista(int idPaciente);




}
