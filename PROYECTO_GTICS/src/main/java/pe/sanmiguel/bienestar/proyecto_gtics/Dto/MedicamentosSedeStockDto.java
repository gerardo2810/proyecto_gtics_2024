package pe.sanmiguel.bienestar.proyecto_gtics.Dto;


import jakarta.persistence.Lob;

import java.util.Base64;

public interface MedicamentosSedeStockDto {

    int getIdMedicamento();
    String getNombre();
    String getUnidad();

    String getDescripcion();

    String getCategorias();

    String getComponente();

    Float getPrecioCompra();

    Float getPrecioVenta();

    Integer getRecetable();

    byte[] getImagen();

    int getIdSede();

    int getCantidad();


}
