package pe.sanmiguel.bienestar.proyecto_gtics.Repository;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.sanmiguel.bienestar.proyecto_gtics.Dto.MedicamentoBajoStockDto;
import pe.sanmiguel.bienestar.proyecto_gtics.Dto.MedicamentosSedeStockDto;
import pe.sanmiguel.bienestar.proyecto_gtics.Dto.VentasMedicamentosTotalDto;
import pe.sanmiguel.bienestar.proyecto_gtics.Dto.VentasMedicamentosporSedeDto;
import pe.sanmiguel.bienestar.proyecto_gtics.Entity.Medicamento;
import pe.sanmiguel.bienestar.proyecto_gtics.Entity.Usuario;

import java.util.List;

@Repository
public interface MedicamentoRepository extends JpaRepository<Medicamento, Integer> {


    @Query("SELECT m FROM Medicamento m WHERE m.idMedicamento = :idMedicamento")
    Medicamento findMedicamentoImageById(@Param("idMedicamento") Integer idMedicamento);

    @Query(nativeQuery = true, value = "SELECT m.idMedicamento, m.nombre, m.unidad, m.descripcion, m.categorias, m.componente, m.precioCompra, m.precioVenta, m.recetable, m.imagen, ss.idSede, ss.cantidad FROM proyecto_gtics.medicamento m inner join sede_stock ss on m.idMedicamento = ss.idMedicamento where ss.idSede = ?1")
    List<MedicamentosSedeStockDto> listarMedicamentosSedeStock(int sedeId);

    @Query(nativeQuery = true, value = "SELECT m.idMedicamento, m.nombre, m.unidad, m.descripcion, m.categorias, m.componente, m.precioCompra, m.precioVenta, m.recetable, m.imagen, ss.idSede, ss.cantidad FROM proyecto_gtics.medicamento m INNER JOIN proyecto_gtics.sede_stock ss ON m.idMedicamento = ss.idMedicamento WHERE ss.cantidad < 25 and  ss.idSede = ?1 and ss.cantidad != 0")
    List<MedicamentosSedeStockDto> findMedicamentosLowStock(int sedeId);

    @Query(nativeQuery = true, value = "SELECT m.idMedicamento, m.nombre, m.unidad, m.descripcion, m.categorias, m.componente, m.precioCompra, m.precioVenta, m.recetable, m.imagen, ss.idSede, ss.cantidad FROM proyecto_gtics.medicamento m inner join sede_stock ss on m.idMedicamento = ss.idMedicamento where ss.cantidad > 25 and ss.idSede = ?1")
    List<MedicamentosSedeStockDto> listarMedicamentosStockDisponible(int sedeId);

    @Query(nativeQuery = true, value = "SELECT m.idMedicamento, m.nombre, m.unidad, m.descripcion, m.categorias, m.componente, m.precioCompra, m.precioVenta, m.recetable, m.imagen, ss.idSede, ss.cantidad FROM proyecto_gtics.medicamento m inner join sede_stock ss on m.idMedicamento = ss.idMedicamento where ss.cantidad = 0 and ss.idSede = ?1")
    List<MedicamentosSedeStockDto> listarMedicamentosStockAgotados(int sedeId);

    @Query(nativeQuery = true, value = "SELECT m.idMedicamento, m.nombre, m.unidad, m.descripcion, m.categorias, m.componente, m.precioCompra, m.precioVenta, m.recetable, m.imagen, ss.idSede, ss.cantidad FROM proyecto_gtics.medicamento m inner join sede_stock ss on m.idMedicamento = ss.idMedicamento where ss.cantidad < 25 and ss.idSede = ?1 and ss.cantidad != 0")
    List<MedicamentosSedeStockDto> listarMedicamentosStockPorAgotar(int sedeId);

    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = "INSERT INTO proyecto_gtics.sede_stock VALUES (?1,?2,?3);")
    void asignarMedicamentoPorSede(int idSede, int idMedicamento, int cantidad);
    @Query(nativeQuery = true, value = "SELECT m.idMedicamento, m.nombre, m.unidad, m.descripcion, m.categorias, m.componente, m.precioCompra, m.precioVenta, m.recetable, m.imagen, ss.idSede, ss.cantidad FROM proyecto_gtics.medicamento m inner join sede_stock ss on m.idMedicamento = ss.idMedicamento where ss.cantidad < 25 and ss.idSede = ?1")
    List<MedicamentosSedeStockDto> listarMedicamentosStockPorAgotaroAgotados(int sedeId);

    @Query(nativeQuery = true, value = "SELECT * FROM proyecto_gtics.medicamento WHERE estado = 1;")
    List<Medicamento> listarMedicamentosActivos();

    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = "UPDATE proyecto_gtics.medicamento SET estado = 0 WHERE idMedicamento = ?")
    void eliminarMedicamento(int idMedicamento);


    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = "UPDATE proyecto_gtics.medicamento SET imagen = ? WHERE idMedicamento = ?")
    void imagenMedicamento(byte[] foto, int idMedicamento);

    @Query(nativeQuery = true, value = "SELECT m.nombre FROM proyecto_gtics.medicamento m")
    List<String> listarNombresMedicamento();
    @Query(nativeQuery = true, value = "SELECT m.nombre FROM proyecto_gtics.medicamento m WHERE m.idMedicamento <> ?")
    List<String> listarNombresMedicamentoID(int idMedicamento);

    @Query(nativeQuery = true, value = "SELECT * FROM proyecto_gtics.medicamento WHERE idMedicamento = ?")
    Medicamento imagenMedicamento(int idMedicamento);

    //Para reportes:
    @Query(nativeQuery = true, value = "SELECT o.idMedicamento, m.nombre, m.unidad, m.precioCompra, m.precioVenta, SUM(o.cantidad) AS totalCantidad, SUM(o.cantidad * (m.precioVenta - m.precioCompra)) AS gananciaTotal FROM orden_contenido o INNER JOIN medicamento m ON o.idMedicamento = m.idMedicamento GROUP BY o.idMedicamento, m.nombre, m.unidad, m.precioCompra, m.precioVenta")
    List<VentasMedicamentosTotalDto> listaMedicamentosVentas();

    @Query(nativeQuery = true, value = "SELECT oc.idMedicamento, m.nombre, m.unidad, m.precioCompra, m.precioVenta, SUM(oc.cantidad) as cantidadVendida, (m.precioVenta - m.precioCompra) * SUM(oc.cantidad) AS ganancia FROM proyecto_gtics.orden o inner join orden_contenido oc on o.id = oc.idOrden inner join medicamento m on m.idMedicamento = oc.idMedicamento where o.idSede = ?1 group by oc.idMedicamento order by cantidadVendida desc")
    List<VentasMedicamentosporSedeDto> listarVentasMedicamentosporSede(int idSede);


    @Query(nativeQuery = true, value = "SELECT m.* FROM proyecto_gtics.sede_stock s, proyecto_gtics.medicamento m, proyecto_gtics.sede p\n" +
            "WHERE s.idMedicamento = m.idMedicamento and p.id=s.idSede and p.nombre=?")
    List<Medicamento> listarMedicamentoSede(String nombreMedicamento);

    List<Medicamento> findByCategorias(String categoria);

    @Query(nativeQuery = true, value = "SELECT ss.idSede, ss.idMedicamento, ss.cantidad, m.nombre FROM proyecto_gtics.sede_stock ss inner join medicamento m on ss.idMedicamento = m.idMedicamento where ss.idSede = ? and cantidad < 25")
    List<MedicamentoBajoStockDto> listarMedicamentosBajoStockxSede(int idSede);



}
