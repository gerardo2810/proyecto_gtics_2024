package pe.sanmiguel.bienestar.proyecto_gtics.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.sanmiguel.bienestar.proyecto_gtics.Entity.Medicamento;
import pe.sanmiguel.bienestar.proyecto_gtics.Entity.Sede;
import pe.sanmiguel.bienestar.proyecto_gtics.Entity.SedeStock;
import pe.sanmiguel.bienestar.proyecto_gtics.Entity.SedeStockId;

import java.util.List;
import java.util.Optional;

@Repository
public interface SedeStockRepository extends JpaRepository<SedeStock, SedeStockId> {

    Optional<SedeStock> getSedeStockByIdSedeAndIdMedicamento(Sede sede, Medicamento medicamento);

    SedeStock getSedeStockByIdMedicamentoAndIdSede(Medicamento medicamento, Sede sede);

}
