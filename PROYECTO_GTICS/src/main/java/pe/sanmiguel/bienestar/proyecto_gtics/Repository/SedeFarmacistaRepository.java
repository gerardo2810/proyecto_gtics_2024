package pe.sanmiguel.bienestar.proyecto_gtics.Repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pe.sanmiguel.bienestar.proyecto_gtics.Dto.UsuarioSedeFarmacistaDto;
import pe.sanmiguel.bienestar.proyecto_gtics.Entity.Sede;
import pe.sanmiguel.bienestar.proyecto_gtics.Entity.SedeFarmacista;
import pe.sanmiguel.bienestar.proyecto_gtics.Entity.SedeFarmacistaId;

import java.util.List;
import java.util.Optional;

@Repository
public interface SedeFarmacistaRepository extends JpaRepository<SedeFarmacista, SedeFarmacistaId> {

    //Actualiza codigo Medico del Farmacista
    @Query(nativeQuery = true, value = "update sede_farmacista set codigoMed = ?1 where idFarmacista = ?2")
    void actualizarCodigoMed(String codigoMed, int id);

    @Query(nativeQuery = true, value = "SELECT * FROM sede_farmacista where idFarmacista = ?1")
    Optional<SedeFarmacista> buscarCodigoFarmacista(int id);

    //Eliminar farmacista de sede_farmacista
    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = "delete from sede_farmacista where idFarmacista = ?1")
    void eliminarFarmacistadeSedeFarmacista(int id);

    //Crear Relacion Sede-Farmacista
    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = "insert into sede_farmacista values (?1, ?2, ?3, ?4, null)")
    void crearSedeFarmacista(int id, int idFarmacista, String codigoMed, int aprobado);

    //Listar tabla SedeFarmacista
    @Query(nativeQuery = true, value = "select * from sede_farmacista")
    List<SedeFarmacista> listarSedeFarmacistas();

    //Seleccionar Sede-Farmacista


    @Query(nativeQuery = true, value = "SELECT * FROM proyecto_gtics.sede_farmacista f INNER JOIN proyecto_gtics.usuario u, proyecto_gtics.sede s WHERE f.idSede = s.id and f.idFarmacista=u.id and f.aprobado=1 and u.estado_usuario =1;")
    List<SedeFarmacista> listarFarmacistasPorSede();

    @Query(nativeQuery = true, value = "SELECT * FROM proyecto_gtics.sede_farmacista f, proyecto_gtics.usuario u WHERE f.idFarmacista = u.id and f.idFarmacista = ?1")
    List<SedeFarmacista> findFarmacistaEnSede(int idFarmacista);

    @Query(nativeQuery = true, value = "SELECT * FROM proyecto_gtics.sede_farmacista f INNER JOIN proyecto_gtics.usuario u, proyecto_gtics.sede s WHERE f.idSede = s.id and f.idFarmacista=u.id and f.aprobado=2;")
    List<SedeFarmacista> listarSolicitudesFarmacistas();

    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = "UPDATE proyecto_gtics.sede_farmacista SET aprobado = 3 WHERE idFarmacista = ?1")
    void denegarSolicitud(Integer idFarmacista);

    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = "UPDATE proyecto_gtics.sede_farmacista SET aprobado = 1 WHERE idFarmacista = ?1")
    void aprobarSolicitud(Integer idFarmacista);

    @Query(nativeQuery = true, value = "SELECT * FROM proyecto_gtics.sede_farmacista f INNER JOIN proyecto_gtics.usuario u WHERE f.idFarmacista=u.id and (f.aprobado=1 or f.aprobado=3);")
    List<SedeFarmacista> listarSolicitudesAceptadasyRechazadas();

    @Query(nativeQuery = true, value = "SELECT * FROM sede_farmacista where idFarmacista = ?1")
    SedeFarmacista buscarFarmacistaSede(int id);

    @Query(nativeQuery = true, value = "SELECT * FROM proyecto_gtics.sede_farmacista f INNER JOIN proyecto_gtics.usuario u, proyecto_gtics.sede s WHERE f.idSede = s.id and f.idFarmacista=u.id and f.aprobado=1 and (u.estado_usuario =1 or u.estado_usuario =2);")
    List<SedeFarmacista> listarFarmacistasActivosInactivos();

}
