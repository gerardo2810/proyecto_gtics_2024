package pe.sanmiguel.bienestar.proyecto_gtics.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pe.sanmiguel.bienestar.proyecto_gtics.Entity.OrdenContenido;
import pe.sanmiguel.bienestar.proyecto_gtics.Entity.ReposicionContenido;
import pe.sanmiguel.bienestar.proyecto_gtics.Entity.ReposicionContenidoId;

import java.util.List;

public interface ReposicionContenidoRepository extends JpaRepository<ReposicionContenido, ReposicionContenidoId> {

    @Query(value="SELECT * FROM proyecto_gtics.reposicion_contenido WHERE idReposicion = ?1", nativeQuery = true)
    List<ReposicionContenido> buscarMedicamentosByReposicionId(String id);
}
