package pe.sanmiguel.bienestar.proyecto_gtics.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pe.sanmiguel.bienestar.proyecto_gtics.Dto.MedicamentosSedeStockDto;
import pe.sanmiguel.bienestar.proyecto_gtics.Entity.Medicamento;

import java.util.List;

@Repository
public interface MedicamentoRepository extends JpaRepository<Medicamento, Integer> {

    /*@Query("SELECT MAX(m.idMedicamento) FROM Medicamento m")
    Integer findLastMedicamentoId();*/

    @Query(nativeQuery = true, value = "SELECT m.idMedicamento, m.nombre, m.unidad, m.descripcion, m.categorias, m.componente, m.precioCompra, m.precioVenta, m.recetable, m.imagen, ss.idSede, ss.cantidad FROM proyecto_gtics.medicamento m inner join sede_stock ss on m.idMedicamento = ss.idMedicamento where ss.idSede = ?1")
    List<MedicamentosSedeStockDto> listarMedicamentosSedeStock(int sedeId);





}
