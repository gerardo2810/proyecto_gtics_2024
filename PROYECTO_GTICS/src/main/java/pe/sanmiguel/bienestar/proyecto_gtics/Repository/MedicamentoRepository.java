package pe.sanmiguel.bienestar.proyecto_gtics.Repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pe.sanmiguel.bienestar.proyecto_gtics.Dto.MedicamentosSedeStockDto;
import pe.sanmiguel.bienestar.proyecto_gtics.Entity.Medicamento;

import java.util.List;

@Repository
public interface MedicamentoRepository extends JpaRepository<Medicamento, Integer> {

    @Query(nativeQuery = true, value = "SELECT MAX(m.idMedicamento) FROM proyecto_gtics.medicamento m;")
    Integer findLastMedicamentoId();

    @Query(nativeQuery = true, value = "SELECT m.idMedicamento, m.nombre, m.unidad, m.descripcion, m.categorias, m.componente, m.precioCompra, m.precioVenta, m.recetable, m.imagen, ss.idSede, ss.cantidad FROM proyecto_gtics.medicamento m inner join sede_stock ss on m.idMedicamento = ss.idMedicamento where ss.idSede = ?1")
    List<MedicamentosSedeStockDto> listarMedicamentosSedeStock(int sedeId);

    @Query(nativeQuery = true, value = "SELECT m.idMedicamento, m.nombre, m.unidad, m.descripcion, m.categorias, m.componente, m.precioCompra, m.precioVenta, m.recetable, m.imagen, ss.idSede, ss.cantidad FROM proyecto_gtics.medicamento m INNER JOIN proyecto_gtics.sede_stock ss ON m.idMedicamento = ss.idMedicamento WHERE ss.cantidad < 5")
    List<MedicamentosSedeStockDto> findMedicamentosLowStock();

    @Query(nativeQuery = true, value = "SELECT m.idMedicamento, m.nombre, m.unidad, m.descripcion, m.categorias, m.componente, m.precioCompra, m.precioVenta, m.recetable, m.imagen, ss.idSede, ss.cantidad FROM proyecto_gtics.medicamento m inner join sede_stock ss on m.idMedicamento = ss.idMedicamento where ss.cantidad > 25 and ss.idSede = ?1")
    List<MedicamentosSedeStockDto> listarMedicamentosStockDisponible(int sedeId);

    @Query(nativeQuery = true, value = "SELECT m.idMedicamento, m.nombre, m.unidad, m.descripcion, m.categorias, m.componente, m.precioCompra, m.precioVenta, m.recetable, m.imagen, ss.idSede, ss.cantidad FROM proyecto_gtics.medicamento m inner join sede_stock ss on m.idMedicamento = ss.idMedicamento where ss.cantidad = 0 and ss.idSede = ?1")
    List<MedicamentosSedeStockDto> listarMedicamentosStockAgotados(int sedeId);

    @Query(nativeQuery = true, value = "SELECT m.idMedicamento, m.nombre, m.unidad, m.descripcion, m.categorias, m.componente, m.precioCompra, m.precioVenta, m.recetable, m.imagen, ss.idSede, ss.cantidad FROM proyecto_gtics.medicamento m inner join sede_stock ss on m.idMedicamento = ss.idMedicamento where ss.cantidad < 25 and ss.idSede = ?1")
    List<MedicamentosSedeStockDto> listarMedicamentosStockPorAgotar(int sedeId);

    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = "INSERT INTO proyecto_gtics.sede_stock VALUES (?1,?2,?3);")
    void asignarMedicamentoPorSede(int idSede, int idMedicamento, int cantidad);

}
