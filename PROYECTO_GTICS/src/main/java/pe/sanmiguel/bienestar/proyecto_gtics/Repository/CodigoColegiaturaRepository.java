package pe.sanmiguel.bienestar.proyecto_gtics.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pe.sanmiguel.bienestar.proyecto_gtics.Entity.CodigoColegiatura;

import java.util.List;

public interface CodigoColegiaturaRepository extends JpaRepository<CodigoColegiatura, Integer> {

    @Query(nativeQuery = true, value = "SELECT * FROM proyecto_gtics.codigo_colegiatura")
    List<CodigoColegiatura> listarCodigos();




}
