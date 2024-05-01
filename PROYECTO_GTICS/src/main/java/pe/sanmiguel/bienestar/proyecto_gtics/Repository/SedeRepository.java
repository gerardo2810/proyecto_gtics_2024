package pe.sanmiguel.bienestar.proyecto_gtics.Repository;


import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pe.sanmiguel.bienestar.proyecto_gtics.Entity.Sede;
import pe.sanmiguel.bienestar.proyecto_gtics.Entity.SedeFarmacista;

import java.util.List;
import java.util.Optional;

@Repository
public interface SedeRepository extends JpaRepository<Sede, Integer> {

    Sede getSedeByIdSede(Integer id);

    @Query(nativeQuery = true, value = "SELECT s.*, u.correo, u.nombres, u.apellidos, u.celular, u.correo FROM proyecto_gtics.sede s, proyecto_gtics.usuario u WHERE s.idAdmin = u.id and u.idRol = 2;")
    List<Sede> listarAdministroresSede();

    @Query(nativeQuery = true, value = "SELECT * FROM proyecto_gtics.sede WHERE idAdmin is null;")
    List<Sede> listarSedesDisponibles();


    @Transactional
    @Modifying
    @Query(value = "UPDATE proyecto_gtics.sede SET idAdmin=?1 WHERE id=?2", nativeQuery = true)
    void asignarAdministradorSede(Integer idAdmin, Integer idsede);

    @Query(nativeQuery = true, value = "SELECT * FROM proyecto_gtics.sede WHERE idAdmin = ?1")
    Optional<Sede> buscarAdminID(Integer idAdmin);
}
