package pe.sanmiguel.bienestar.proyecto_gtics.Dto;


public interface MedicamentosSedeStockDto {

    int getIdMedicamento();
    String getNombreMedicamento();
    String getUnidadMedicamento();

    String getDescripcion();

    String getCategorias();

    String getComponente();

    Float getPrecioCompra();

    Float getPrecioVenta();

    Integer getRecetable();

    String getImagen();

    int getIdSede();

    int getCantidad();


}
