package pe.sanmiguel.bienestar.proyecto_gtics.Repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pe.sanmiguel.bienestar.proyecto_gtics.Entity.SedeFarmacista;
import pe.sanmiguel.bienestar.proyecto_gtics.Entity.SedeFarmacistaId;

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
    @Query(nativeQuery = true, value = "insert into sede_farmacista values (?1, ?2, ?3, ?4)")
    void crearSedeFarmacista(int id, int idFarmacista, String codigoMed, int aprobado);


}
