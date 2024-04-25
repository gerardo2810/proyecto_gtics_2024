package pe.sanmiguel.bienestar.proyecto_gtics.Repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.sanmiguel.bienestar.proyecto_gtics.Entity.Sede;

@Repository
public interface SedeRepository extends JpaRepository<Sede, Integer> {

    Sede getSedeByIdSede(Integer id);

}
