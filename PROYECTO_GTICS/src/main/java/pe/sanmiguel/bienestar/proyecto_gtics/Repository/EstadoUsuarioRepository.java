package pe.sanmiguel.bienestar.proyecto_gtics.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pe.sanmiguel.bienestar.proyecto_gtics.Entity.EstadoUsuario;
import pe.sanmiguel.bienestar.proyecto_gtics.Entity.Usuario;

import java.util.List;

public interface EstadoUsuarioRepository extends JpaRepository<EstadoUsuario, Integer> {

    @Query(nativeQuery = true, value = "SELECT * FROM proyecto_gtics.estado_usuario WHERE id = 1 or id = 2;")
    List<EstadoUsuario> listarEstadosUsuarios();
}
