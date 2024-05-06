package pe.sanmiguel.bienestar.proyecto_gtics.Repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pe.sanmiguel.bienestar.proyecto_gtics.Entity.SedeDoctor;
import pe.sanmiguel.bienestar.proyecto_gtics.Entity.SedeDoctorId;
import pe.sanmiguel.bienestar.proyecto_gtics.Entity.SedeFarmacista;

import java.util.List;

@Repository
public interface SedeDoctorRepository extends JpaRepository<SedeDoctor, SedeDoctorId> {

    @Query(nativeQuery = true, value = "SELECT sd.*, s.nombre FROM proyecto_gtics.sede_doctor sd, proyecto_gtics.sede s, proyecto_gtics.doctor d WHERE sd.idDoctor=d.id and sd.idSede = s.id and sd.idDoctor=?")
    List<SedeDoctor> listarSedesDondeEstaDoctor(int idDoctor);

    @Query(nativeQuery = true, value = "SELECT m.idSede FROM proyecto_gtics.sede_doctor m WHERE idDoctor = ?")
    List<Integer> listarDoctoresEnSedePorId(int idDoctor);

    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = "DELETE FROM proyecto_gtics.sede_doctor WHERE idDoctor = ?")
    void borrarSedesAnterioresDoctor(int idDoctor);

    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = "INSERT INTO proyecto_gtics.sede_doctor VALUES (?1,?2)")
    void asignarDoctorPorSede(int idDoctor, int idSede);
}
