package pe.sanmiguel.bienestar.proyecto_gtics.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.sanmiguel.bienestar.proyecto_gtics.Entity.EstadoOrden;

public interface EstadoOrdenRepository extends JpaRepository<EstadoOrden, Integer> {
}
