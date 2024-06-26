package pe.sanmiguel.bienestar.proyecto_gtics.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pe.sanmiguel.bienestar.proyecto_gtics.Dto.OrdenOrdenContenidoDto;
import pe.sanmiguel.bienestar.proyecto_gtics.Dto.TopVentasDto;
import pe.sanmiguel.bienestar.proyecto_gtics.Entity.OrdenContenido;
import pe.sanmiguel.bienestar.proyecto_gtics.Entity.OrdenContenidoId;

import java.util.List;

@Repository
public interface OrdenContenidoRepository extends JpaRepository<OrdenContenido, OrdenContenidoId> {

    /*ArrayList<OrdenContenido> findAllByIdOrden(Integer idOrden);

    @Query("SELECT MAX(oc.idEntrada) FROM OrdenContenido oc")
    Integer findLastOrdenContenidoId();*/

    @Query(value="select * from orden_contenido where idOrden = ?1", nativeQuery = true)
    List<OrdenContenido> findMedicamentosByOrdenId(String id);



    @Query(value="SELECT SUM(cantidad) FROM proyecto_gtics.orden_contenido where idOrden = ?1", nativeQuery = true)
    Integer cantProductos(String id);

    @Query(value = "SELECT o.id, o.idSede, oc.cantidad FROM orden o inner join orden_contenido oc on o.id = oc.idOrden where idSede = ?1", nativeQuery = true)
    List<OrdenOrdenContenidoDto> listarOrdenContenidoTotalporSede(int idSede);

    @Query(nativeQuery = true, value = "SELECT oc.idMedicamento, SUM(oc.cantidad) as cantidadVendida FROM proyecto_gtics.orden o inner join orden_contenido oc on o.id = oc.idOrden where o.idSede = ? group by oc.idMedicamento order by cantidadVendida desc limit 0, 5")
    List<TopVentasDto> listartopVentarporSede(int idSede);

    //Para reportes en Admin de sede
    @Query(nativeQuery = true, value = "SELECT oc.idMedicamento, SUM(oc.cantidad) as cantidadVendida FROM proyecto_gtics.orden o inner join orden_contenido oc on o.id = oc.idOrden where o.idSede = ? group by oc.idMedicamento order by cantidadVendida desc")
    List<TopVentasDto> listarVentasporSede(int idSede);


}
