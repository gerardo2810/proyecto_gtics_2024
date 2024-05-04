package pe.sanmiguel.bienestar.proyecto_gtics.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.sanmiguel.bienestar.proyecto_gtics.Entity.EstadoUsuario;

public interface EstadoUsuarioRepository extends JpaRepository<EstadoUsuario, Integer> {
}
