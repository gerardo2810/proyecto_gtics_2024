package pe.sanmiguel.bienestar.proyecto_gtics.Dto;

import jakarta.persistence.criteria.CriteriaBuilder;

public interface ReposicionContenidoMedicamentoDto {

    Integer getIdMedicamento();

    Integer getIdReposicion();

    Integer getCantidad();

    String getNombre();

    String getUnidad();

    Float getPrecioCompra();

    String getImagen();


}
