package pe.sanmiguel.bienestar.proyecto_gtics.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
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


}
