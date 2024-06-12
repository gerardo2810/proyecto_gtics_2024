package pe.sanmiguel.bienestar.proyecto_gtics.util.reportes;


import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfTable;
import com.lowagie.text.pdf.PdfWriter;
import jakarta.servlet.http.HttpServletResponse;
import pe.sanmiguel.bienestar.proyecto_gtics.Entity.Medicamento;

import java.awt.*;
import java.io.IOException;
import java.util.List;

public class ExporterPDF {

    private List<Medicamento> listaMedicamentos;

    public ExporterPDF(List<Medicamento> listaMedicamentos) {
        this.listaMedicamentos = listaMedicamentos;
    }

    private void escribirCabeceraDeLaTabla(PdfPTable tabla){

        PdfPCell celda = new PdfPCell();

        celda.setBackgroundColor(Color.decode("#0d6efd"));

        celda.setPadding(5);

        Font fuente = FontFactory.getFont(FontFactory.HELVETICA);
        fuente.setColor(Color.WHITE);

        celda.setPhrase(new Phrase("ID", fuente));
        tabla.addCell(celda);

        celda.setPhrase(new Phrase("Nombre", fuente));
        tabla.addCell(celda);

        celda.setPhrase(new Phrase("Número de unidad", fuente));
        tabla.addCell(celda);

        celda.setPhrase(new Phrase("Unidad", fuente));
        tabla.addCell(celda);

        celda.setPhrase(new Phrase("Categoría", fuente));
        tabla.addCell(celda);

        celda.setPhrase(new Phrase("Precio de compra", fuente));
        tabla.addCell(celda);

        celda.setPhrase(new Phrase("Precio de venta", fuente));
        tabla.addCell(celda);
    }

    private void escribirDatosDeLaTabla(PdfPTable tabla){

        for(Medicamento medicamento: listaMedicamentos){
            tabla.addCell(String.valueOf(medicamento.getIdMedicamento()));
            tabla.addCell(medicamento.getNombre());
            tabla.addCell(String.valueOf(medicamento.getNumunidad()));
            tabla.addCell(medicamento.getUnidad());
            tabla.addCell(medicamento.getCategorias());
            tabla.addCell(String.valueOf(medicamento.getPrecioCompra()));
            tabla.addCell(String.valueOf(medicamento.getPrecioVenta()));
        }
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
        Chunk tituloChunk = new Chunk("Lista de medicamentos", fuente);
        tituloChunk.setUnderline(1f, -2f);  // grosor y desplazamiento del subrayado

        // Crear un Paragraph y añadir el Chunk
        Paragraph titulo = new Paragraph(tituloChunk);
        titulo.setAlignment(Paragraph.ALIGN_CENTER);

        String imagePath = "src/main/resources/static/media/logo_main.png"; // Reemplaza con la ruta a tu imagen
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

        // Añadir espacio adicional para evitar superposición
        Paragraph espacio = new Paragraph(" ");
        espacio.setSpacingBefore(80); // Ajusta el espacio según sea necesario
        documento.add(espacio);


        // Añadir el título al documento
        documento.add(titulo);

        // Añadir espacio adicional para evitar superposición
        Paragraph espacio2 = new Paragraph(" ");
        espacio2.setSpacingBefore(20); // Ajusta el espacio según sea necesario
        documento.add(espacio2);

        PdfPTable tabla = new PdfPTable(7);
        tabla.setWidthPercentage(100);
        tabla.setSpacingBefore(15);
        tabla.setWidths(new float[] {1f, 3f, 1.5f, 4f, 1.8f,1.5f,1.5f});
        tabla.setWidthPercentage(110);

        escribirCabeceraDeLaTabla(tabla);
        escribirDatosDeLaTabla(tabla);

        documento.add(tabla);
        documento.close();

    }



}
