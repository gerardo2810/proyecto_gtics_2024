package pe.sanmiguel.bienestar.proyecto_gtics.util.reportes;


import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfTable;
import com.lowagie.text.pdf.PdfWriter;
import jakarta.servlet.http.HttpServletResponse;
import pe.sanmiguel.bienestar.proyecto_gtics.Dto.OrdenOrdenContenidoDto;
import pe.sanmiguel.bienestar.proyecto_gtics.Dto.VentasMedicamentosTotalDto;
import pe.sanmiguel.bienestar.proyecto_gtics.Entity.Medicamento;

import java.awt.*;
import java.io.IOException;
import java.util.List;

public class ExporterPDF {

    //Exportar Lista de Ventas de Medicamentos Super Admin

    private List<VentasMedicamentosTotalDto> listaMedicamentos;
    private List<VentasMedicamentosTotalDto> listaMedicamentosSede1;
    private List<VentasMedicamentosTotalDto> listaMedicamentosSede2;
    private List<VentasMedicamentosTotalDto> listaMedicamentosSede3;
    private List<VentasMedicamentosTotalDto> listaMedicamentosSede4;
    private List<VentasMedicamentosTotalDto> listaMedicamentosSede5;


    public ExporterPDF(List<VentasMedicamentosTotalDto> listaMedicamentos) {
        this.listaMedicamentos = listaMedicamentos;
    }

    private void escribirCabeceraDeLaTabla(PdfPTable tabla){

        PdfPCell celda = new PdfPCell();

        celda.setBackgroundColor(Color.decode("#0d6efd"));

        celda.setPadding(5);

        Font fuente = FontFactory.getFont(FontFactory.HELVETICA);
        fuente.setColor(Color.WHITE);

        celda.setPhrase(new Phrase("Nombre", fuente));
        tabla.addCell(celda);

        celda.setPhrase(new Phrase("Unidad", fuente));
        tabla.addCell(celda);

        celda.setPhrase(new Phrase("Precio de compra", fuente));
        tabla.addCell(celda);

        celda.setPhrase(new Phrase("Precio de venta", fuente));
        tabla.addCell(celda);

        celda.setPhrase(new Phrase("Cantidad vendida", fuente));
        tabla.addCell(celda);

        celda.setPhrase(new Phrase("Ganancia", fuente));
        tabla.addCell(celda);
    }

    private void escribirDatosDeLaTabla(PdfPTable tabla){

        for(VentasMedicamentosTotalDto medicamento: listaMedicamentos){
            tabla.addCell(crearCeldaCentrada(medicamento.getNombre()));
            tabla.addCell(crearCeldaCentrada(medicamento.getUnidad()));
            tabla.addCell(crearCeldaCentrada(String.valueOf(medicamento.getPrecioCompra())));
            tabla.addCell(crearCeldaCentrada(String.valueOf(medicamento.getPrecioVenta())));
            tabla.addCell(crearCeldaCentrada(String.valueOf(medicamento.getTotalCantidad())));
            tabla.addCell(crearCeldaCentrada(String.valueOf(medicamento.getGananciaTotal())));
        }
    }

    private PdfPCell crearCeldaCentrada(String texto) {
        PdfPCell celda = new PdfPCell(new Phrase(texto));
        celda.setHorizontalAlignment(Element.ALIGN_CENTER);
        celda.setVerticalAlignment(Element.ALIGN_MIDDLE);
        return celda;
    }

    public void exportar(HttpServletResponse response) throws DocumentException, IOException {
        Document documento = new Document(PageSize.A4);
        PdfWriter.getInstance(documento, response.getOutputStream());

        documento.open();

        Font fuente = FontFactory.getFont(FontFactory.HELVETICA_BOLD);

        Color color = Color.decode("#000000");
        fuente.setColor(color);
        fuente.setSize(18);

        // Crear un Chunk con subrayado
        Chunk tituloChunk = new Chunk("Lista de ventas de medicamentos", fuente);
        tituloChunk.setUnderline(1f, -2f);  // grosor y desplazamiento del subrayado

        // Crear un Paragraph y añadir el Chunk
        Paragraph titulo = new Paragraph(tituloChunk);
        titulo.setAlignment(Paragraph.ALIGN_CENTER);

        String imagePath = "src/main/resources/static/media/logo_main.png"; // poner la ruta
        Image imagen = Image.getInstance(imagePath);

        //Escalamos la imagen:
        imagen.scaleToFit(180, 180);

        // Posicionar la imagen
        float imageWidth = imagen.getScaledWidth();
        float documentWidth = documento.getPageSize().getWidth();
        float margin = documento.leftMargin();
        float xPos = documentWidth - imageWidth - margin;
        float yPos = documento.getPageSize().getHeight() - imagen.getScaledHeight() - margin;

        imagen.setAbsolutePosition(xPos, yPos);
        documento.add(imagen);

        // Añade espacio adicional para evitar superposición
        Paragraph espacio = new Paragraph(" ");
        espacio.setSpacingBefore(80); // Ajusta el espacio según sea necesario
        documento.add(espacio);


        // Añade el título al documento
        documento.add(titulo);

        // Añade espacio adicional para evitar superposición
        Paragraph espacio2 = new Paragraph(" ");
        espacio2.setSpacingBefore(20); // Ajusta el espacio según sea necesario
        documento.add(espacio2);

        PdfPTable tabla = new PdfPTable(6);
        tabla.setWidthPercentage(100);
        tabla.setSpacingBefore(15);
        tabla.setWidths(new float[] {3f, 3f, 2f, 1.8f,1.5f,1.7f});
        tabla.setWidthPercentage(110);

        escribirCabeceraDeLaTabla(tabla);
        escribirDatosDeLaTabla(tabla);

        documento.add(tabla);
        documento.close();

    }

    //Exportar Lista de cantidades de ventas realizadas en todas las sedes

    //Primero extraemos las ganancias de cada sede:












}
