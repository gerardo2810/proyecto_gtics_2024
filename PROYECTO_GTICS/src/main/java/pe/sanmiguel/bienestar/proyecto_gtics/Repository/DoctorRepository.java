package pe.sanmiguel.bienestar.proyecto_gtics.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.sanmiguel.bienestar.proyecto_gtics.Entity.Doctor;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Integer> {
    Doctor getByIdDoctor(Integer id);

}
