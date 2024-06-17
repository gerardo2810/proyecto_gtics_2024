package pe.sanmiguel.bienestar.proyecto_gtics.Repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import pe.sanmiguel.bienestar.proyecto_gtics.Entity.Chat;
import pe.sanmiguel.bienestar.proyecto_gtics.Entity.Doctor;

import java.util.List;

public interface ChatRepository extends JpaRepository<Chat, Integer> {


    @Query(nativeQuery = true, value = "SELECT * FROM proyecto_gtics.chat where idUser1=?;")
    List<Chat> listaChatsParaPaciente(int idPaciente);

    @Query(nativeQuery = true, value = "SELECT * FROM proyecto_gtics.chat where idUser2=?;")
    List<Chat> listaChatsParaFarmacista(int idPaciente);

    @Query(value="SELECT MAX(idChat) FROM chat", nativeQuery = true)
    Integer findLastChatId();

    @Query(value="SELECT * FROM chat where idUser1=? and idUser2=?", nativeQuery = true)
    Chat buscarChat(int idUser1, int idUser2);

    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = "INSERT INTO proyecto_gtics.chat VALUES (?1,?2,?3);")
    void crearChat(int idChat, int idUser1, int idUser2);




}
