package pe.sanmiguel.bienestar.proyecto_gtics.Repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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


    @Query(nativeQuery = true, value = "SELECT cantidad FROM proyecto_gtics.sede_stock where idSede =?1 and idMedicamento = ?2")
    Integer verificarCantidadStockPorSede(int idSede, int idMedicamento);

    //Aumentamos la cantidad de stock en la sede

    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = "UPDATE sede_stock SET cantidad = cantidad + ?3 WHERE idSede = ?1 and idMedicamento = ?2")
    void actualizarSedeStock(int idSede, int idMedicamento, Integer cantidadAumentada);


    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = "UPDATE sede_stock SET cantidad = cantidad - ?3 WHERE idSede = ?1 and idMedicamento = ?2")
    void reducirStockPorSede(int idSede, int idMedicamento, Integer cantidad);

    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = "UPDATE sede_stock SET cantidad = cantidad + ?3 WHERE idSede = ?1 and idMedicamento = ?2")
    void aumentarStockPorSede(int idSede, int idMedicamento, Integer cantidad);



    @Query("SELECT m.idMedicamento AS idMedicamento, m.nombre AS nombre, m.unidad AS unidad, m.descripcion AS descripcion, m.categorias AS categorias, m.componente AS componente, m.precioCompra AS precioCompra, m.precioVenta AS precioVenta, m.recetable AS recetable, m.imagen AS imagen, SUM(s.cantidad) AS cantidad " +
            "FROM SedeStock s " +
            "JOIN s.idMedicamento m " +
            "GROUP BY m.idMedicamento, m.nombre, m.unidad, m.descripcion, m.categorias, m.componente, m.precioCompra, m.precioVenta, m.recetable, m.imagen")
    List<MedicamentosSedeStockDto> findMedicamentosConStock();


    @Query("SELECT m.idMedicamento AS idMedicamento, m.nombre AS nombre, m.unidad AS unidad, m.descripcion AS descripcion, " +
            "m.categorias AS categorias, m.componente AS componente, m.precioCompra AS precioCompra, m.precioVenta AS precioVenta, " +
            "m.recetable AS recetable, m.imagen AS imagen, SUM(s.cantidad) AS cantidad " +
            "FROM SedeStock s " +
            "JOIN s.idMedicamento m " +
            "WHERE m.categorias LIKE %:categoria% " + // Filtrar por categoría usando LIKE
            "GROUP BY m.idMedicamento, m.nombre, m.unidad, m.descripcion, m.categorias, m.componente, m.precioCompra, m.precioVenta, m.recetable, m.imagen")
    List<MedicamentosSedeStockDto> findMedicamentosConStockByCategoria(@Param("categoria") String categoria);



    @Query("SELECT m.idMedicamento AS idMedicamento, m.nombre AS nombre, m.unidad AS unidad, m.descripcion AS descripcion, " +
            "m.categorias AS categorias, m.componente AS componente, m.precioCompra AS precioCompra, m.precioVenta AS precioVenta, " +
            "m.recetable AS recetable, m.imagen AS imagen, s.idSede.idSede AS idSede, SUM(s.cantidad) AS cantidad " +
            "FROM SedeStock s " +
            "JOIN s.idMedicamento m " +
            "WHERE s.idSede.idSede = :idSede " +
            "GROUP BY m.idMedicamento, m.nombre, m.unidad, m.descripcion, m.categorias, m.componente, m.precioCompra, " +
            "m.precioVenta, m.recetable, m.imagen, s.idSede.idSede")
    List<MedicamentosSedeStockDto> findMedicamentosConStockBySede(@Param("idSede") Integer idSede);

}
