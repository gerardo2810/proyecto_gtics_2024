package pe.sanmiguel.bienestar.proyecto_gtics.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pe.sanmiguel.bienestar.proyecto_gtics.Entity.Reposicion;
import pe.sanmiguel.bienestar.proyecto_gtics.Entity.SedeFarmacista;

import java.util.List;

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

}
