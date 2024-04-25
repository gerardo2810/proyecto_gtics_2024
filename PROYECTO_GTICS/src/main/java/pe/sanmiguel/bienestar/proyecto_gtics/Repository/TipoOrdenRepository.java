package pe.sanmiguel.bienestar.proyecto_gtics.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.sanmiguel.bienestar.proyecto_gtics.Entity.TipoOrden;

public interface TipoOrdenRepository extends JpaRepository<TipoOrden, Integer> {
}
