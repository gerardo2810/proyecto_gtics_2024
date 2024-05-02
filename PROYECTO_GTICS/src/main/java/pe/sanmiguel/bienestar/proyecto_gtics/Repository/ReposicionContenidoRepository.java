package pe.sanmiguel.bienestar.proyecto_gtics.Repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import pe.sanmiguel.bienestar.proyecto_gtics.Dto.ReposicionContenidoMedicamentoDto;
import pe.sanmiguel.bienestar.proyecto_gtics.Entity.OrdenContenido;
import pe.sanmiguel.bienestar.proyecto_gtics.Entity.ReposicionContenido;
import pe.sanmiguel.bienestar.proyecto_gtics.Entity.ReposicionContenidoId;

import java.util.List;

public interface ReposicionContenidoRepository extends JpaRepository<ReposicionContenido, ReposicionContenidoId> {

    @Query(value="SELECT * FROM proyecto_gtics.reposicion_contenido WHERE idReposicion = ?1", nativeQuery = true)
    List<ReposicionContenido> buscarMedicamentosByReposicionId(String id);

    // Guardar lista de medicamentos de una reposición
    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = "insert into reposicion_contenido set idMedicamento = ?1, idReposicion = ?2, cantidad = ?3")
    void guardarContenidoReposicion(int idMedicamento, int idReposicion, int cantidad);

    //Listar DTO para ver Detalles de Compra

    @Query(nativeQuery = true, value = "SELECT rc.idMedicamento, rc.idReposicion, rc.cantidad, m.nombre, m.unidad, m.precioCompra FROM proyecto_gtics.reposicion_contenido rc inner join medicamento m on rc.idMedicamento = m.idMedicamento where idReposicion = ?1")
    List<ReposicionContenidoMedicamentoDto> listaMostrarDetalleNuevaCompra(int idReposicion);

    //Eliminar Contenido de reposición
    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = "delete from reposicion_contenido where idReposicion = ?1")
    void eliminarContenidoReposicion(int idReposicion);

}
