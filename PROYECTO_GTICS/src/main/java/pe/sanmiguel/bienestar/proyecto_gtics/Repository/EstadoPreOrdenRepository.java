package pe.sanmiguel.bienestar.proyecto_gtics.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.sanmiguel.bienestar.proyecto_gtics.Entity.EstadoPreOrden;
import pe.sanmiguel.bienestar.proyecto_gtics.Entity.EstadoReposicion;

@Repository
public interface EstadoPreOrdenRepository extends JpaRepository<EstadoPreOrden,Integer> {
}
