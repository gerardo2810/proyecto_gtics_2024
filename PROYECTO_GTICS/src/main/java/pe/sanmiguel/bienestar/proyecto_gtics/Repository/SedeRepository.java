package pe.sanmiguel.bienestar.proyecto_gtics.Repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pe.sanmiguel.bienestar.proyecto_gtics.Entity.Sede;
import pe.sanmiguel.bienestar.proyecto_gtics.Entity.Usuario;

import java.util.List;

@Repository
public interface SedeRepository extends JpaRepository<Sede, Integer> {

    Sede getSedeByIdSede(Integer id);

    @Query(nativeQuery = true, value = "SELECT s.*, u.correo, u.nombres, u.apellidos, u.celular, u.correo FROM proyecto_gtics.sede s, proyecto_gtics.usuario u WHERE s.idAdmin = u.id and u.idRol = 1;")
    List<Sede> listarAdministroresSede();



}
