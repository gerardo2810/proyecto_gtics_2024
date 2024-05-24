package pe.sanmiguel.bienestar.proyecto_gtics.Repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pe.sanmiguel.bienestar.proyecto_gtics.Entity.Doctor;

import java.util.List;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Integer> {
    Doctor getByIdDoctor(Integer id);

    @Query(nativeQuery = true, value = "SELECT * from doctor")
    List<Doctor> listarDoctores();

    @Query(nativeQuery = true, value = "SELECT * FROM doctor d inner join sede_doctor sd on d.id = sd.idDoctor where sd.idSede = ?")
    List<Doctor> listarDoctoresporSede(int idSede);

    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = "DELETE FROM proyecto_gtics.doctor WHERE id = ?")
    void eliminarDoctor(int idDoctor);

}
