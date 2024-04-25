package pe.sanmiguel.bienestar.proyecto_gtics.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pe.sanmiguel.bienestar.proyecto_gtics.Entity.OrdenContenido;
import pe.sanmiguel.bienestar.proyecto_gtics.Entity.OrdenContenidoId;

import java.util.ArrayList;
import java.util.List;

@Repository
public interface OrdenContenidoRepository extends JpaRepository<OrdenContenido, OrdenContenidoId> {

    /*ArrayList<OrdenContenido> findAllByIdOrden(Integer idOrden);

    @Query("SELECT MAX(oc.idEntrada) FROM OrdenContenido oc")
    Integer findLastOrdenContenidoId();*/

    @Query(value="select * from orden_contenido where idOrden = ?1", nativeQuery = true)
    List<OrdenContenido> findMedicamentosByOrdenId(String id);
}
