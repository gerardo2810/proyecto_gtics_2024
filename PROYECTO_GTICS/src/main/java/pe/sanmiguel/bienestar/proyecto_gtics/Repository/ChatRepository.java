package pe.sanmiguel.bienestar.proyecto_gtics.Repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import pe.sanmiguel.bienestar.proyecto_gtics.Entity.Chat;
import pe.sanmiguel.bienestar.proyecto_gtics.Entity.Doctor;

import java.util.List;

public interface ChatRepository extends JpaRepository<Chat, Integer> {


    @Query(nativeQuery = true, value = "SELECT JSON_OBJECT(\n" +
            "    'type', type,\n" +
            "    'project_id', project_id,\n" +
            "    'private_key_id', private_key_id,\n" +
            "    'private_key', private_key,\n" +
            "    'client_email', client_email,\n" +
            "    'client_id', client_id,\n" +
            "    'auth_uri', auth_uri,\n" +
            "    'token_uri', token_uri,\n" +
            "    'auth_provider_x509_cert_url', auth_provider_x509_cert_url,\n" +
            "    'client_x509_cert_url', client_x509_cert_url,\n" +
            "    'universe_domain', universe_domain\n" +
            ") AS json_result\n" +
            "FROM firebase_json\n" +
            "WHERE id = 1;")
    String firebaseJSON();

    @Query(nativeQuery = true, value = "SELECT * FROM proyecto_gtics.chat where idUser1=?;")
    List<Chat> listaChatsParaPaciente(int idPaciente);

    @Query(nativeQuery = true, value = "SELECT * FROM proyecto_gtics.chat where idUser2=?;")
    List<Chat> listaChatsParaFarmacista(int idPaciente);

    @Query(value="SELECT MAX(idChat) FROM chat", nativeQuery = true)
    Integer findLastChatId();

    @Query(value="SELECT * FROM chat where idUser1=? and idUser2=? and idOrden=?", nativeQuery = true)
    Chat buscarChat(int idUser1, int idUser2, int idOrden);

    @Query(value="SELECT * FROM chat where  idUser2=? and idOrden=?", nativeQuery = true)
    Chat buscarChatReemplazarMed(int idUser2, int idOrden);

    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = "INSERT INTO proyecto_gtics.chat VALUES (?1,?2,?3,?4);")
    void crearChat(int idChat, int idUser1, int idUser2, int idOrden);




}
