package pe.sanmiguel.bienestar.proyecto_gtics.util.reportes;


import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import pe.sanmiguel.bienestar.proyecto_gtics.Dto.VentasMedicamentosTotalDto;
import pe.sanmiguel.bienestar.proyecto_gtics.Dto.TopVentasDto;
import pe.sanmiguel.bienestar.proyecto_gtics.Entity.Medicamento;
import pe.sanmiguel.bienestar.proyecto_gtics.Repository.MedicamentoRepository;

import java.awt.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Optional;

public class ExporterPDF {

    private final MedicamentoRepository medicamentoRepository;

    //Exportar Lista de Ventas de Medicamentos Super Admin

    private List<VentasMedicamentosTotalDto> listaMedicamentos;
    private List<TopVentasDto> listaVentastopSede1;
    private List<TopVentasDto> listaVentastopSede2;
    private List<TopVentasDto> listaVentastopSede3;
    private List<TopVentasDto> listaVentastopSede4;
    private List<TopVentasDto> listaVentastopSede5;


    public ExporterPDF(List<VentasMedicamentosTotalDto> listaMedicamentos, List<TopVentasDto> listaVentastopSede1, List<TopVentasDto> listaVentastopSede2, List<TopVentasDto> listaVentastopSede3, List<TopVentasDto> listaVentastopSede4, List<TopVentasDto> listaVentastopSede5, MedicamentoRepository medicamentoRepository) {
        this.listaMedicamentos = listaMedicamentos;
        this.listaVentastopSede1 = listaVentastopSede1;
        this.listaVentastopSede2 = listaVentastopSede2;
        this.listaVentastopSede3 = listaVentastopSede3;
        this.listaVentastopSede4 = listaVentastopSede4;
        this.listaVentastopSede5 = listaVentastopSede5;
        this.medicamentoRepository = medicamentoRepository;
    }

    private void escribirCabeceraDeLaTabla(PdfPTable tabla) {
        Font fuente = FontFactory.getFont(FontFactory.HELVETICA);
        fuente.setColor(Color.WHITE);

        agregarCeldaCabecera(tabla, "Nombre", fuente);
        agregarCeldaCabecera(tabla, "Unidad", fuente);
        agregarCeldaCabecera(tabla, "Precio de compra", fuente);
        agregarCeldaCabecera(tabla, "Precio de venta", fuente);
        agregarCeldaCabecera(tabla, "Cantidad vendida", fuente);
        agregarCeldaCabecera(tabla, "Ganancia", fuente);
    }

    private void agregarCeldaCabecera(PdfPTable tabla, String texto, Font fuente) {
        PdfPCell celda = new PdfPCell(new Phrase(texto, fuente));
        celda.setBackgroundColor(Color.decode("#0d6efd"));
        celda.setPadding(5);
        celda.setHorizontalAlignment(Element.ALIGN_CENTER);
        tabla.addCell(celda);
    }

    private void escribirDatosDeLaTabla(PdfPTable tabla){

        DecimalFormat df = new DecimalFormat("#0.00");
        DecimalFormat df2 = new DecimalFormat("#0");

        for (VentasMedicamentosTotalDto medicamento : listaMedicamentos) {
            tabla.addCell(crearCeldaCentrada(medicamento.getNombre()));
            tabla.addCell(crearCeldaCentrada(medicamento.getUnidad()));
            tabla.addCell(crearCeldaCentrada(df.format(medicamento.getPrecioCompra())));
            tabla.addCell(crearCeldaCentrada(df.format(medicamento.getPrecioVenta())));
            tabla.addCell(crearCeldaCentrada(df2.format(medicamento.getTotalCantidad())));
            tabla.addCell(crearCeldaCentrada(df.format(medicamento.getGananciaTotal())));
        }
    }

    private void escribirDatosDeLaTablaSede1(PdfPTable tabla){

        for(TopVentasDto topVentasDto: listaVentastopSede1){

            //Extraigo el medicamento:

            Optional<Medicamento> optmedicamento = medicamentoRepository.findById(topVentasDto.getIdMedicamento());

            if(optmedicamento.isPresent()){
                Medicamento medicamento = optmedicamento.get();
                tabla.addCell(crearCeldaCentrada(medicamento.getNombre()));
                tabla.addCell(crearCeldaCentrada(medicamento.getUnidad()));
                tabla.addCell(crearCeldaCentrada(String.valueOf(medicamento.getPrecioCompra())));
                tabla.addCell(crearCeldaCentrada(String.valueOf(medicamento.getPrecioVenta())));
                tabla.addCell(crearCeldaCentrada(String.valueOf(topVentasDto.getCantidadVendida())));
                BigDecimal m = medicamento.getPrecioCompra();
                BigDecimal n = medicamento.getPrecioVenta();
                tabla.addCell(crearCeldaCentrada(String.valueOf(new BigDecimal(topVentasDto.getCantidadVendida()).multiply(n.subtract(m)))));

            }else {
                System.out.println("No hay medicamento");
            }
        }

    }

    private void escribirDatosDeLaTablaSede2(PdfPTable tabla){

        for(TopVentasDto topVentasDto: listaVentastopSede2){

            //Extraigo el medicamento:

            Optional<Medicamento> optmedicamento = medicamentoRepository.findById(topVentasDto.getIdMedicamento());

            if(optmedicamento.isPresent()){
                Medicamento medicamento = optmedicamento.get();
                tabla.addCell(crearCeldaCentrada(medicamento.getNombre()));
                tabla.addCell(crearCeldaCentrada(medicamento.getUnidad()));
                tabla.addCell(crearCeldaCentrada(String.valueOf(medicamento.getPrecioCompra())));
                tabla.addCell(crearCeldaCentrada(String.valueOf(medicamento.getPrecioVenta())));
                tabla.addCell(crearCeldaCentrada(String.valueOf(topVentasDto.getCantidadVendida())));
                BigDecimal m = medicamento.getPrecioCompra();
                BigDecimal n = medicamento.getPrecioVenta();
                tabla.addCell(crearCeldaCentrada(String.valueOf(new BigDecimal(topVentasDto.getCantidadVendida()).multiply(n.subtract(m)))));

            }else {
                System.out.println("No hay medicamento");
            }
        }

    }

    private void escribirDatosDeLaTablaSede3(PdfPTable tabla){

        for(TopVentasDto topVentasDto: listaVentastopSede3){

            //Extraigo el medicamento:

            Optional<Medicamento> optmedicamento = medicamentoRepository.findById(topVentasDto.getIdMedicamento());

            if(optmedicamento.isPresent()){
                Medicamento medicamento = optmedicamento.get();
                tabla.addCell(crearCeldaCentrada(medicamento.getNombre()));
                tabla.addCell(crearCeldaCentrada(medicamento.getUnidad()));
                tabla.addCell(crearCeldaCentrada(String.valueOf(medicamento.getPrecioCompra())));
                tabla.addCell(crearCeldaCentrada(String.valueOf(medicamento.getPrecioVenta())));
                tabla.addCell(crearCeldaCentrada(String.valueOf(topVentasDto.getCantidadVendida())));
                BigDecimal m = medicamento.getPrecioCompra();
                BigDecimal n = medicamento.getPrecioVenta();
                tabla.addCell(crearCeldaCentrada(String.valueOf(new BigDecimal(topVentasDto.getCantidadVendida()).multiply(n.subtract(m)))));

            }else {
                System.out.println("No hay medicamento");
            }
        }

    }

    private void escribirDatosDeLaTablaSede4(PdfPTable tabla){

        for(TopVentasDto topVentasDto: listaVentastopSede4){

            //Extraigo el medicamento:

            Optional<Medicamento> optmedicamento = medicamentoRepository.findById(topVentasDto.getIdMedicamento());

            if(optmedicamento.isPresent()){
                Medicamento medicamento = optmedicamento.get();
                tabla.addCell(crearCeldaCentrada(medicamento.getNombre()));
                tabla.addCell(crearCeldaCentrada(medicamento.getUnidad()));
                tabla.addCell(crearCeldaCentrada(String.valueOf(medicamento.getPrecioCompra())));
                tabla.addCell(crearCeldaCentrada(String.valueOf(medicamento.getPrecioVenta())));
                tabla.addCell(crearCeldaCentrada(String.valueOf(topVentasDto.getCantidadVendida())));
                BigDecimal m = medicamento.getPrecioCompra();
                BigDecimal n = medicamento.getPrecioVenta();
                tabla.addCell(crearCeldaCentrada(String.valueOf(new BigDecimal(topVentasDto.getCantidadVendida()).multiply(n.subtract(m)))));

            }else {
                System.out.println("No hay medicamento");
            }
        }

    }

    private void escribirDatosDeLaTablaSede5(PdfPTable tabla){

        for(TopVentasDto topVentasDto: listaVentastopSede5){

            //Extraigo el medicamento:

            Optional<Medicamento> optmedicamento = medicamentoRepository.findById(topVentasDto.getIdMedicamento());

            if(optmedicamento.isPresent()){
                Medicamento medicamento = optmedicamento.get();
                tabla.addCell(crearCeldaCentrada(medicamento.getNombre()));
                tabla.addCell(crearCeldaCentrada(medicamento.getUnidad()));
                tabla.addCell(crearCeldaCentrada(String.valueOf(medicamento.getPrecioCompra())));
                tabla.addCell(crearCeldaCentrada(String.valueOf(medicamento.getPrecioVenta())));
                tabla.addCell(crearCeldaCentrada(String.valueOf(topVentasDto.getCantidadVendida())));
                BigDecimal m = medicamento.getPrecioCompra();
                BigDecimal n = medicamento.getPrecioVenta();
                tabla.addCell(crearCeldaCentrada(String.valueOf(new BigDecimal(topVentasDto.getCantidadVendida()).multiply(n.subtract(m)))));

            }else {
                System.out.println("No hay medicamento");
            }
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
        espacio.setSpacingBefore(15); // Ajusta el espacio según sea necesario
        documento.add(espacio);

        PdfPTable tabla = new PdfPTable(6);
        tabla.setWidthPercentage(100);
        tabla.setSpacingBefore(15);
        tabla.setWidths(new float[] {3f, 3f, 2f, 1.8f,1.5f,1.7f});
        tabla.setWidthPercentage(110);

        escribirCabeceraDeLaTabla(tabla);
        escribirDatosDeLaTabla(tabla);

        documento.add(tabla);

        // Añade espacio adicional para evitar superposición
        espacio.setSpacingBefore(20); // Ajusta el espacio según sea necesario
        documento.add(espacio);

        // Crear un Chunk con subrayado
        Chunk tituloChunk2 = new Chunk("Top 5 ventas de medicamentos en la Sede 1", fuente);
        tituloChunk2.setUnderline(1f, -2f);  // grosor y desplazamiento del subrayado

        // Crear un Paragraph y añadir el Chunk
        Paragraph titulo2 = new Paragraph(tituloChunk2);
        titulo2.setAlignment(Paragraph.ALIGN_CENTER);

        documento.add(titulo2);

        // Añade espacio adicional para evitar superposición
        espacio.setSpacingBefore(20); // Ajusta el espacio según sea necesario
        documento.add(espacio);

        PdfPTable tabla2 = new PdfPTable(6);
        tabla2.setWidthPercentage(100);
        tabla2.setSpacingBefore(15);
        tabla2.setWidths(new float[] {3f, 3f, 2f, 1.8f,1.5f,1.7f});
        tabla2.setWidthPercentage(110);

        escribirCabeceraDeLaTabla(tabla2);
        escribirDatosDeLaTablaSede1(tabla2);

        documento.add(tabla2);

        // Añade espacio adicional para evitar superposición
        espacio.setSpacingBefore(20); // Ajusta el espacio según sea necesario
        documento.add(espacio);

        // Añade espacio adicional para evitar superposición
        espacio.setSpacingBefore(80); // Ajusta el espacio según sea necesario
        documento.add(espacio);

        // Crear un Chunk con subrayado
        Chunk tituloChunk3 = new Chunk("Top 5 ventas de medicamentos en la Sede 2", fuente);
        tituloChunk3.setUnderline(1f, -2f);  // grosor y desplazamiento del subrayado

        // Crear un Paragraph y añadir el Chunk
        Paragraph titulo3 = new Paragraph(tituloChunk3);
        titulo3.setAlignment(Paragraph.ALIGN_CENTER);

        documento.add(titulo3);

        // Crear un Chunk con subrayado
        Chunk tituloChunk4 = new Chunk("Top 5 ventas de medicamentos en la Sede 3", fuente);
        tituloChunk4.setUnderline(1f, -2f);  // grosor y desplazamiento del subrayado

        // Crear un Paragraph y añadir el Chunk
        Paragraph titulo4 = new Paragraph(tituloChunk4);
        titulo4.setAlignment(Paragraph.ALIGN_CENTER);

        documento.add(titulo4);

        // Crear un Chunk con subrayado
        Chunk tituloChunk5 = new Chunk("Top 5 ventas de medicamentos en la Sede 4", fuente);
        tituloChunk5.setUnderline(1f, -2f);  // grosor y desplazamiento del subrayado

        // Crear un Paragraph y añadir el Chunk
        Paragraph titulo5 = new Paragraph(tituloChunk5);
        titulo5.setAlignment(Paragraph.ALIGN_CENTER);

        documento.add(titulo5);

        // Crear un Chunk con subrayado
        Chunk tituloChunk6 = new Chunk("Top 5 ventas de medicamentos en la Sede 5", fuente);
        tituloChunk6.setUnderline(1f, -2f);  // grosor y desplazamiento del subrayado

        // Crear un Paragraph y añadir el Chunk
        Paragraph titulo6 = new Paragraph(tituloChunk6);
        titulo6.setAlignment(Paragraph.ALIGN_CENTER);

        documento.add(titulo6);

        documento.close();

    }

    //Exportar Lista de cantidades de ventas realizadas en todas las sedes

    //Primero extraemos las ganancias de cada sede:












}
