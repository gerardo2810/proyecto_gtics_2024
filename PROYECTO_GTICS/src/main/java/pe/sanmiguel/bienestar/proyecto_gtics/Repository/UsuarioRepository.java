package pe.sanmiguel.bienestar.proyecto_gtics.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pe.sanmiguel.bienestar.proyecto_gtics.Entity.Usuario;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    Optional<Usuario> findByCorreoAndDni(String correo, String dni);

    @Query("SELECT MAX(u.idUsuario) FROM Usuario u")
    Integer findLastUsuarioId();

    //Listado de farmacistas
    @Query(nativeQuery = true, value = "SELECT * FROM proyecto_gtics.usuario where idRol = 3 and estado_usuario = 1")
    List<Usuario> listarFarmacistas();

    //Encontrar Farmacista por ID
    @Query(nativeQuery = true, value = "SELECT * FROM proyecto_gtics.usuario where idRol = 3 and estado_usuario = 1 and id = ?1")
    Usuario encontrarFarmacistaporId(int id);

    //Editar Farmacista
    @Query(nativeQuery = true, value = "update usuario set nombres = ?2, apellidos = ?3, dni = ?4, distrito = ?5, correo = ?6 where id = ?1")
    void editarFarmacista(int id, String nombre, String apellido, String dni, String distrito, String correo);

}
