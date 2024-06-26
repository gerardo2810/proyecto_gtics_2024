package pe.sanmiguel.bienestar.proyecto_gtics.Repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.sanmiguel.bienestar.proyecto_gtics.Entity.Reposicion;
import pe.sanmiguel.bienestar.proyecto_gtics.Entity.SedeFarmacista;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReposicionRepository extends JpaRepository<Reposicion, Integer> {

    @Query(nativeQuery = true, value = "SELECT r.*,\n" +
            "CONCAT(s.id) AS 'SedeID', s.idAdmin, s.direccion, s.nombre,\n" +
            "CONCAT(f.id) AS 'idFarmacista', f.idRol, f.correo, f.contrasena, f.nombres, f.apellidos, f.celular, f.dni, f.direccion\n" +
            "FROM proyecto_gtics.reposicion r, proyecto_gtics.sede s, proyecto_gtics.usuario f WHERE r.idEstado = s.id and s.idAdmin = f.id;")
    List<Reposicion> listarOrdenesReposicion();

    @Query(nativeQuery = true, value = "SELECT r.*,\n" +
            "CONCAT(s.id) AS 'SedeID', s.idAdmin, s.direccion, s.nombre,\n" +
            "CONCAT(f.id) AS 'idFarmacista', f.idRol, f.correo, f.contrasena, f.nombres, f.apellidos, f.celular, f.dni, f.direccion\n" +
            "FROM proyecto_gtics.reposicion r, proyecto_gtics.sede s, proyecto_gtics.usuario f WHERE r.idEstado = s.id and s.idAdmin = f.id and r.id = 1;")
    List<Reposicion> verMasDetallesOrdenesReposicion();

    @Query(nativeQuery = true, value = "SELECT MAX(r.id) FROM reposicion r")
    Optional<Integer> findLastReposicionId();

    @Query(nativeQuery = true, value = "SELECT MAX(r.numero) FROM reposicion r where r.idSede = ?")
    Optional<Integer> findLastNumeroporSede(int idSede);

    // Crear reposicion
    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = "insert into reposicion set id = ?1, tracking = ?2, fechaIni = ?7, fechaFin = ?8, pagado = 0, precioTotal = ?3, idEstado = ?4, idSede = ?5, numero = ?6")
    void crearOrdenReposicion(int idReposicion, String tracking, Float precioTotal, int idEstado, int idSede, int newNumber, LocalDateTime fechaIni, LocalDateTime fechaFin);

    @Query(nativeQuery = true, value = "select * from reposicion where id = ?1")
    Reposicion encontrarReposicionporId(int id);

    //Listar orden de reposicion entregadas CORREGIR CON SESION-------------------------------------
    @Query(nativeQuery = true, value = "select * from reposicion where idEstado = 5 and idSede = ?1")
    List<Reposicion> listarOrdenesReposicionEntregadas(int idSede);

    //Listar órdenes de reposición no entregadas CORREGIR CON SESION-------------------------------------
    @Query(nativeQuery = true, value = "select * from reposicion where idEstado != 5 and idSede = ?1")
    List<Reposicion> listarOrdenesReposicionNoEntregadas(int idSede);

    // Encontrar los últimos 2 reposiciones que no tengan estado 5

    @Query(nativeQuery = true, value = "SELECT MAX(r.id) FROM reposicion r where r.idEstado != 5 and r.idSede = ?")
    Optional<Integer> findLastReposicionIdNoEntregadoporSede(int idSede);

    //Listar últimas dos reposiciones
    @Query(nativeQuery = true, value = "SELECT * FROM proyecto_gtics.reposicion where idEstado != 5 and idSede = ?1 and (numero = ?2 or numero = ?3)")
    List<Reposicion> listarOrdenesReposicionNoEntregadasUltimas(int idSede, int finalNum, int preFinalNum);

    //Eliminar Reposición
    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = "delete from reposicion where id = ?1")
    void eliminarReposicionporId(int idReposicion);


    @Transactional
    @Modifying
    @Query(value = "SET GLOBAL event_scheduler = ON", nativeQuery = true)
    void enableEventScheduler();

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
            "        UPDATE reposicion SET idEstado = idEstado + 1 WHERE id = :ordenId AND idEstado = estado; " + // Actualiza solo para la orden específica y el estado actual
            "        SET estado = estado + 1; " + // Mover al siguiente estado
            "    END WHILE; " +
            "END", nativeQuery = true)
    void createIncrementReposicionStateEvent(@Param("ordenId") Integer ordenId);



}
