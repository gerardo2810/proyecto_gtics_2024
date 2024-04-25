package pe.sanmiguel.bienestar.proyecto_gtics.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.sanmiguel.bienestar.proyecto_gtics.Entity.Reposicion;

@Repository
public interface ReposicionRepository extends JpaRepository<Reposicion, Integer> {

}
