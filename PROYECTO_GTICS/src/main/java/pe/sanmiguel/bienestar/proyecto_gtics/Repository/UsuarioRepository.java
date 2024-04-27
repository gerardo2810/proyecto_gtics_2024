package pe.sanmiguel.bienestar.proyecto_gtics.Repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pe.sanmiguel.bienestar.proyecto_gtics.Dto.UsuarioSedeFarmacistaDto;
import pe.sanmiguel.bienestar.proyecto_gtics.Entity.Usuario;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    Optional<Usuario> findByCorreoAndDni(String correo, String dni);

    @Query("SELECT MAX(u.idUsuario) FROM Usuario u")
    Integer findLastUsuarioId();


    @Query(nativeQuery = true, value = "SELECT * FROM proyecto_gtics.usuario WHERE idRol = ?1")
    List<Usuario> listarUsuariosSegunRol(int id);

    //Listado de farmacistas
    @Query(nativeQuery = true, value = "SELECT * FROM proyecto_gtics.usuario where idRol = 3 and estado_usuario = 1")
    List<Usuario> listarFarmacistas();

    //Encontrar Farmacista por ID
    @Query(nativeQuery = true, value = "SELECT * FROM proyecto_gtics.usuario where idRol = 3 and estado_usuario = 1 and id = ?1")
    Usuario encontrarFarmacistaporId(int id);

    //Editar Farmacista
    @Query(nativeQuery = true, value = "update usuario set nombres = ?2, apellidos = ?3, dni = ?4, distrito = ?5, correo = ?6 where id = ?1")
    void editarFarmacista(int id, String nombre, String apellido, String dni, String distrito, String correo);

    //Eliminar Farmacista
    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = "delete from usuario where id = ?1")
    void eliminarFarmacistadeUsuario(int id);

    //Crear Solicitud de Farmacista
    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = "insert into usuario values (?1, ?2, ?3, ?4, ?5, ?6, ?7, ?8, ?9, ?10, ?11, ?12)")
    void crearFarmacista(int id, int idRol, String correo, String contrasena, String nombres, String apellidos, String celular, String dni, String direccion, String distrito, String seguro, int estadoUsuario);

    @Query(nativeQuery = true, value = "SELECT u.id, u.idRol, u.correo, u.contrasena, u.nombres, u.apellidos, u.celular, u.dni, u.direccion, u.distrito, u.seguro, u.estado_usuario, sf.idSede, sf.codigoMed, sf.aprobado FROM proyecto_gtics.usuario u inner join sede_farmacista sf on u.id = sf.idFarmacista where sf.idSede = ?1 and aprobado = 1")
    List<UsuarioSedeFarmacistaDto> listarSedeFarmacista(int idAdminsede);



}
