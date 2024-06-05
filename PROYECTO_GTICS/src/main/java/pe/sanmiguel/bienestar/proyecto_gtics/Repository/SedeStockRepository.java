package pe.sanmiguel.bienestar.proyecto_gtics.Repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pe.sanmiguel.bienestar.proyecto_gtics.Dto.MedicamentosSedeStockDto;
import pe.sanmiguel.bienestar.proyecto_gtics.Entity.*;

import java.util.List;
import java.util.Optional;

@Repository
public interface SedeStockRepository extends JpaRepository<SedeStock, SedeStockId> {

    Optional<SedeStock> getSedeStockByIdSedeAndIdMedicamento(Sede sede, Medicamento medicamento);

    SedeStock getSedeStockByIdMedicamentoAndIdSede(Medicamento medicamento, Sede sede);

    @Query(nativeQuery = true, value = "SELECT m.idSede FROM proyecto_gtics.sede_stock m WHERE idMedicamento = ?")
    List<Integer> listarMedicamentosEnSedePorId(int idMedicamento);

    @Query(nativeQuery = true, value = "SELECT m.idMedicamento FROM proyecto_gtics.sede_stock m WHERE idSede = ?")
    List<Integer> listarIdsMedicamentoporSede(int idSede);

    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = "DELETE FROM proyecto_gtics.sede_stock WHERE idMedicamento = ?")
    void borrarSedesAnteriores(int idMedicamento);

    @Query(nativeQuery = true, value = "SELECT s.*, m.nombre, se.nombre FROM proyecto_gtics.sede_stock s, proyecto_gtics.medicamento m, proyecto_gtics.sede se WHERE s.idMedicamento = m.idMedicamento and s.idSede = se.id and s.idMedicamento = ?")
    List<SedeStock> medicamentoPresenteSedes(int idMedicamento);

}
