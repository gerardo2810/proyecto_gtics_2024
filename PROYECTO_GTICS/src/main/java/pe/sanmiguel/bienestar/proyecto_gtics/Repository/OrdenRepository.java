package pe.sanmiguel.bienestar.proyecto_gtics.Repository;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import pe.sanmiguel.bienestar.proyecto_gtics.Entity.Orden;

import java.util.List;

@Repository
public interface OrdenRepository extends JpaRepository<Orden, Integer> {
    @Query(value = "SELECT o.* FROM orden o JOIN tipo_orden t ON o.idTipo = t.id WHERE t.nombre = 'WEB'", nativeQuery = true)
    List<Orden> findAllOrdenesWeb();
    @Query(value = "SELECT o.* FROM orden o JOIN tipo_orden t ON o.idTipo = t.id WHERE t.nombre != 'PREORDEN'", nativeQuery = true)
    List<Orden> findAllOrdenes();
    @Query(value = "SELECT o.* FROM orden o JOIN tipo_orden t ON o.idTipo = t.id WHERE t.nombre = 'PREORDEN'", nativeQuery = true)
    List<Orden> findAllPreOrdenes();
    @Query(value="SELECT MAX(orden.id) FROM Orden orden", nativeQuery = true)
    Integer findLastOrdenId();

    @Transactional
    @Modifying
    @Query(value="update orden set idEstado=?1 where id=?2", nativeQuery = true)
    void actualizarEstadoOrden(Integer estado, Integer idOrden);


    Orden getOrdenByIdOrden(Integer idOrden);



    Orden findByIdOrdenAndTipoOrden(Integer idOrden, Integer tipoOrden);


    /*@Query("SELECT o FROM Orden o WHERE o.idSede = :idSede")
    List<Orden> findBySedeId(Integer idSede);*/


    @Query(value="select * from orden where idEstado not in (8,9) and idTipo != 3; ", nativeQuery = true)
    List <Orden> listarOrdenes();


    @Query(value="select * from orden where idEstado not in (8,9) and idTipo = 3; ", nativeQuery = true)
    List <Orden> listarPreOrdenes();





    @Transactional
    @Modifying
    @Query(value = "SET GLOBAL event_scheduler = ON", nativeQuery = true)
    void enableEventScheduler();

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
            "        UPDATE orden SET estado_preorden = estado_preorden + 1 WHERE id = :ordenId AND estado_preorden = estado; " + // Actualiza solo para la orden específica y el estado actual
            "        SET estado = estado + 1; " + // Mover al siguiente estado
            "    END WHILE; " +
            "END", nativeQuery = true)
    void createIncrementPreOrderStateEvent(@Param("ordenId") Integer ordenId);


}
