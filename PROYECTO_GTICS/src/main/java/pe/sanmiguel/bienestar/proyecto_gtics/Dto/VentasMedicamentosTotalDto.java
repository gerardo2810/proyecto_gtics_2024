package pe.sanmiguel.bienestar.proyecto_gtics.Dto;

public interface VentasMedicamentosTotalDto {



    int getIdMedicamento();
    String getNombre();

    String getUnidad();

    Float getPrecioCompra();

    Float getPrecioVenta();

    int getTotalCantidad();

    Float getGananciaTotal();


}
