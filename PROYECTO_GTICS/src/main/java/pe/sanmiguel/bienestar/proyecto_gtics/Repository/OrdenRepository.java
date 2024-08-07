package pe.sanmiguel.bienestar.proyecto_gtics.Repository;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import pe.sanmiguel.bienestar.proyecto_gtics.Dto.OrdenCantidadVentasDto;
import pe.sanmiguel.bienestar.proyecto_gtics.Dto.OrdenOrdenContenidoSedeDto;
import pe.sanmiguel.bienestar.proyecto_gtics.Dto.OrdenesExporterDto;
import pe.sanmiguel.bienestar.proyecto_gtics.Entity.Orden;

import java.util.List;

@Repository
public interface OrdenRepository extends JpaRepository<Orden, Integer> {

    @Query(value = "SELECT o.* FROM orden o JOIN tipo_orden t ON o.idTipo = t.id WHERE t.nombre = 'WEB'", nativeQuery = true)
    List<Orden> findAllOrdenesWeb();

    @Query(value = "SELECT o.* FROM orden o JOIN tipo_orden t ON o.idTipo = t.id WHERE t.nombre = 'WEB' AND o.idSede = :idSede AND o.precioTotal != 0", nativeQuery = true)
    List<Orden> findAllOrdenesWebPorSede(@Param("idSede") Integer idSede);

    @Query(value = "SELECT o.* FROM orden o JOIN tipo_orden t ON o.idTipo = t.id WHERE t.nombre != 'PREORDEN'", nativeQuery = true)
    List<Orden> findAllOrdenes();

    @Query(value = "SELECT o.* FROM orden o JOIN tipo_orden t ON o.idTipo = t.id WHERE t.nombre != 'PREORDEN' AND o.idSede = :idSede", nativeQuery = true)
    List<Orden> findAllOrdenesPorSede(@Param("idSede") Integer idSede);

    @Query(value = "SELECT o.* FROM orden o JOIN tipo_orden t ON o.idTipo = t.id WHERE t.nombre = 'PREORDEN'", nativeQuery = true)
    List<Orden> findAllPreOrdenes();

    @Query(value = "SELECT o.* FROM orden o JOIN tipo_orden t ON o.idTipo = t.id WHERE t.nombre = 'PREORDEN' AND o.idSede = :idSede", nativeQuery = true)
    List<Orden> findAllPreOrdenesPorSede(@Param("idSede") Integer idSede);

    @Query(value="SELECT MAX(orden.id) FROM orden orden", nativeQuery = true)
    Integer findLastOrdenId();

    @Transactional
    @Modifying
    @Query(value="update orden set idEstado=?1 where id=?2", nativeQuery = true)
    void actualizarEstadoOrden(Integer estado, Integer idOrden);


    Orden getOrdenByIdOrden(Integer idOrden);

    @Query(nativeQuery = true, value = "SELECT * FROM orden WHERE orden.id = :idOrden")
    Orden findPreordenById(@Param("idOrden") Integer idOrden);

    @Query(nativeQuery = true, value = "SELECT o2.* FROM orden o1 LEFT JOIN orden o2 ON o1.id = o2.idOrden WHERE o1.id = :idOrden")
    Orden findPreordenByOrdenId(@Param("idOrden") Integer idOrden);


    Orden findByIdOrdenAndTipoOrden(Integer idOrden, Integer tipoOrden);


    /*@Query("SELECT o FROM Orden o WHERE o.idSede = :idSede")
    List<Orden> findBySedeId(Integer idSede);*/


    @Query(value = "SELECT * FROM orden WHERE idEstado NOT IN (8) AND idTipo = 2 AND idPaciente = ?1", nativeQuery = true)
    List<Orden> listarOrdenes(Integer idPaciente);

    @Query(value = "SELECT * FROM orden WHERE idEstado NOT IN (8) AND idTipo = 2 AND idPaciente = ?1 AND precioTotal != 0", nativeQuery = true)
    List<Orden> listarOrdenesPriceNoZero(Integer idPaciente);

    @Query(value = "SELECT * FROM orden WHERE idEstado NOT IN (8, 9) AND idTipo = 3 AND idPaciente = ?1", nativeQuery = true)
    List<Orden> listarPreOrdenes(Integer idPaciente);

    @Query(value = "SELECT * FROM orden WHERE id=?1 AND idPaciente = ?2", nativeQuery = true)
    Orden ordenXusuario(Integer idOrden, Integer idPaciente);

    @Query(value = "SELECT * FROM orden WHERE id=?1 AND idPaciente = ?2 and idEstado not in(1,2)", nativeQuery = true)
    Orden ordenesPagadas(Integer idOrden, Integer idPaciente);

    @Query(value = "SELECT * FROM orden WHERE id=?1 AND idPaciente = ?2 AND idEstado= 2", nativeQuery = true)
    Orden ordenFaltaPago(Integer idOrden, Integer idPaciente);





    @Transactional
    @Modifying
    @Query(value = "SET GLOBAL event_scheduler = ON", nativeQuery = true)
    void enableEventScheduler();

    @Transactional
    @Modifying
    @Query(value = "delete from orden where id=?", nativeQuery = true)
    void eliminarOrden(String idOrden);


    /*Para Pre Ordenes*/

    @Transactional
    @Modifying
    @Query(value = "CREATE EVENT IncrementPreOrderStateEvent " +
            "ON SCHEDULE EVERY 2 MINUTE " + // Cambiado a 2 minutos para cubrir los 5 estados en 10 minutos
            "STARTS CURRENT_TIMESTAMP " +
            "ENDS CURRENT_TIMESTAMP + INTERVAL 10 MINUTE " + // Duración total de 10 minutos
            "DO " +
            "BEGIN " +
            "    DECLARE estado INT; " +
            "    SET estado = 1; " +
            "    WHILE estado <= 5 DO " + // 5 estados en total
            "        UPDATE orden SET estadoPreorden = estadoPreorden + 1 WHERE id = :ordenId AND estadoPreorden = estado; " + // Actualiza solo para la orden específica y el estado actual
            "        SET estado = estado + 1; " + // Mover al siguiente estado
            "    END WHILE; " +
            "END", nativeQuery = true)
    void createIncrementPreOrderStateEvent(@Param("ordenId") Integer ordenId);


    /*Para Ordenes Web*/

    @Transactional
    @Modifying
    @Query(value = "CREATE EVENT IncrementPreOrderStateEvent " +
            "ON SCHEDULE EVERY 2 MINUTE " + // Cambiado a 2 minutos para cubrir los 5 estados en 10 minutos
            "STARTS CURRENT_TIMESTAMP " +
            "ENDS CURRENT_TIMESTAMP + INTERVAL 10 MINUTE " + // Duración total de 10 minutos
            "DO " +
            "BEGIN " +
            "    DECLARE estado INT; " +
            "    SET estado = 3; " +
            "    WHILE estado <= 7 DO " + // 5 estados en total
            "        UPDATE orden SET idEstado = idEstado + 1 WHERE id = :ordenId AND idEstado = estado; " + // Actualiza solo para la orden específica y el estado actual
            "        SET estado = estado + 1; " + // Mover al siguiente estado
            "    END WHILE; " +
            "END", nativeQuery = true)
    void createIncrementOrderWebStateEvent(@Param("ordenId") Integer ordenId);

    @Query(nativeQuery = true, value = "SELECT o.id, o.precioTotal, idSede, idMedicamento FROM orden o inner join orden_contenido oc on o.id = oc.idOrden where idSede = ?")
    List<OrdenOrdenContenidoSedeDto> listaOrdenOrdenContenidoporSede(int idSede);

    @Query(nativeQuery = true, value = "SELECT o.id, o.fechaIni, o.fechaFin, o.precioTotal, u.nombres, u.apellidos, s.nombre as nombreSede, o.idTipo FROM orden o inner join usuario u on o.idPaciente = u.id inner join sede s on o.idSede = s.id;")
    List<OrdenesExporterDto> listarOrdenesExporter();

    @Query(nativeQuery = true, value = "SELECT idSede, count(*) as cantidadOrdenes FROM orden group by idSede")
    List<OrdenCantidadVentasDto> listarCantidadVentasEnSedes();






}
