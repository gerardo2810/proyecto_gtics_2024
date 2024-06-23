package pe.sanmiguel.bienestar.proyecto_gtics.util.reportes;


import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import pe.sanmiguel.bienestar.proyecto_gtics.Dto.VentasMedicamentosTotalDto;
import pe.sanmiguel.bienestar.proyecto_gtics.Dto.TopVentasDto;
import pe.sanmiguel.bienestar.proyecto_gtics.Entity.Medicamento;
import pe.sanmiguel.bienestar.proyecto_gtics.Repository.MedicamentoRepository;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
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

    private Float gananciaTotal = 0.0f;
    private BigDecimal gananciaTotalsede1 = BigDecimal.valueOf(0);
    private BigDecimal gananciaTotalsede2 = BigDecimal.valueOf(0);
    private BigDecimal gananciaTotalsede3 = BigDecimal.valueOf(0);
    private BigDecimal gananciaTotalsede4 = BigDecimal.valueOf(0);
    private BigDecimal gananciaTotalsede5 = BigDecimal.valueOf(0);




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

    private void agregarFilaTotal(Document document, String texto1, String texto2) throws DocumentException {
        PdfPTable tablaTotal = new PdfPTable(2);
        tablaTotal.setWidthPercentage(50); // Ajusta el ancho según sea necesario
        tablaTotal.setSpacingBefore(10f); // Espaciado antes de la tabla

        // Fuentes
        Font fuenteBlanca = FontFactory.getFont(FontFactory.HELVETICA);
        fuenteBlanca.setColor(Color.WHITE);

        Font fuenteNegra = FontFactory.getFont(FontFactory.HELVETICA);
        fuenteNegra.setColor(Color.BLACK);

        // Celda 1: "Ganancia Total"
        PdfPCell celda1 = new PdfPCell(new Phrase(texto1, fuenteBlanca));
        celda1.setBackgroundColor(Color.decode("#0d6efd"));
        celda1.setPadding(5);
        celda1.setHorizontalAlignment(Element.ALIGN_CENTER);
        tablaTotal.addCell(celda1);

        // Celda 2: Valor de la ganancia
        PdfPCell celda2 = new PdfPCell(new Phrase(texto2, fuenteNegra));
        celda2.setBackgroundColor(Color.WHITE);
        celda2.setPadding(5);
        celda2.setHorizontalAlignment(Element.ALIGN_CENTER);
        tablaTotal.addCell(celda2);

        // Agregar la tabla al documento
        document.add(tablaTotal);
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
            gananciaTotal = gananciaTotal + medicamento.getGananciaTotal();


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
                gananciaTotalsede1 = gananciaTotalsede1.add(new BigDecimal(topVentasDto.getCantidadVendida()).multiply(n.subtract(m)));


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
                gananciaTotalsede2 = gananciaTotalsede2.add(new BigDecimal(topVentasDto.getCantidadVendida()).multiply(n.subtract(m)));
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
                gananciaTotalsede3 = gananciaTotalsede3.add(new BigDecimal(topVentasDto.getCantidadVendida()).multiply(n.subtract(m)));
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
                gananciaTotalsede4 = gananciaTotalsede4.add(new BigDecimal(topVentasDto.getCantidadVendida()).multiply(n.subtract(m)));
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
                gananciaTotalsede5 = gananciaTotalsede5.add(new BigDecimal(topVentasDto.getCantidadVendida()).multiply(n.subtract(m)));
                System.out.println(gananciaTotalsede5);
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

        ClassPathResource imgFile = new ClassPathResource("static/media/logo_main.png");
        InputStream inputStream = imgFile.getInputStream();
        Image imagen = Image.getInstance(inputStream.readAllBytes());
        inputStream.close();

        //String imagePath = "static/media/logo_main.png"; // poner la ruta
        //ruta: img/media/logo_main.png
        //Image imagen = Image.getInstance(imagePath);

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

        documento.add(espacio);

        agregarFilaTotal(documento, "Ganancia total", String.valueOf(gananciaTotal));

        // Añade espacio adicional para evitar superposición
        espacio.setSpacingBefore(20); // Ajusta el espacio según sea necesario
        documento.add(espacio);
        documento.add(espacio);
        documento.add(espacio);
        documento.add(espacio);

        // Crear un Chunk con subrayado
        Chunk tituloChunk2 = new Chunk("Medicamentos más vendidos en Sede Central", fuente);
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
        espacio.setSpacingBefore(15); // Ajusta el espacio según sea necesario
        documento.add(espacio);

        agregarFilaTotal(documento, "Ganancia total", String.valueOf(gananciaTotalsede1));

        // Añade espacio adicional para evitar superposición
        espacio.setSpacingBefore(80); // Ajusta el espacio según sea necesario
        documento.add(espacio);

        documento.add(imagen);
        espacio.setSpacingBefore(20);

        // Crear un Chunk con subrayado
        Chunk tituloChunk3 = new Chunk("Medicamentos más vendidos en Sede Norte", fuente);
        tituloChunk3.setUnderline(1f, -2f);  // grosor y desplazamiento del subrayado

        // Crear un Paragraph y añadir el Chunk
        Paragraph titulo3 = new Paragraph(tituloChunk3);
        titulo3.setAlignment(Paragraph.ALIGN_CENTER);

        documento.add(titulo3);

        documento.add(espacio);

        PdfPTable tabla3 = new PdfPTable(6);
        tabla3.setWidthPercentage(100);
        tabla3.setSpacingBefore(15);
        tabla3.setWidths(new float[] {3f, 3f, 2f, 1.8f,1.5f,1.7f});
        tabla3.setWidthPercentage(110);

        escribirCabeceraDeLaTabla(tabla3);
        escribirDatosDeLaTablaSede2(tabla3);

        documento.add(tabla3);

        documento.add(espacio);

        agregarFilaTotal(documento, "Ganancia total", String.valueOf(gananciaTotalsede2));

        documento.add(espacio);
        documento.add(espacio);

        // Crear un Chunk con subrayado
        Chunk tituloChunk4 = new Chunk("Medicamentos más vendidos en Sede Sur", fuente);
        tituloChunk4.setUnderline(1f, -2f);  // grosor y desplazamiento del subrayado

        // Crear un Paragraph y añadir el Chunk
        Paragraph titulo4 = new Paragraph(tituloChunk4);
        titulo4.setAlignment(Paragraph.ALIGN_CENTER);

        documento.add(titulo4);

        documento.add(espacio);

        PdfPTable tabla4 = new PdfPTable(6);
        tabla4.setWidthPercentage(100);
        tabla4.setSpacingBefore(15);
        tabla4.setWidths(new float[] {3f, 3f, 2f, 1.8f,1.5f,1.7f});
        tabla4.setWidthPercentage(110);

        escribirCabeceraDeLaTabla(tabla4);
        escribirDatosDeLaTablaSede3(tabla4);
        documento.add(tabla4);
        espacio.setSpacingBefore(20);
        documento.add(espacio);
        agregarFilaTotal(documento, "Ganancia total", String.valueOf(gananciaTotalsede3));
        espacio.setSpacingBefore(60);
        documento.add(espacio);
        documento.add(imagen);
        espacio.setSpacingBefore(20);

        // Crear un Chunk con subrayado
        Chunk tituloChunk5 = new Chunk("Medicamentos más vendidos en Sede Este", fuente);
        tituloChunk5.setUnderline(1f, -2f);  // grosor y desplazamiento del subrayado

        // Crear un Paragraph y añadir el Chunk
        Paragraph titulo5 = new Paragraph(tituloChunk5);
        titulo5.setAlignment(Paragraph.ALIGN_CENTER);

        documento.add(titulo5);

        documento.add(espacio);

        PdfPTable tabla5 = new PdfPTable(6);
        tabla5.setWidthPercentage(100);
        tabla5.setSpacingBefore(15);
        tabla5.setWidths(new float[] {3f, 3f, 2f, 1.8f,1.5f,1.7f});
        tabla5.setWidthPercentage(110);

        escribirCabeceraDeLaTabla(tabla5);
        escribirDatosDeLaTablaSede4(tabla5);

        documento.add(tabla5);

        documento.add(espacio);

        agregarFilaTotal(documento, "Ganancia total", String.valueOf(gananciaTotalsede4));

        espacio.setSpacingBefore(20);
        documento.add(espacio);
        documento.add(imagen);
        documento.add(espacio);

        // Crear un Chunk con subrayado
        Chunk tituloChunk6 = new Chunk("Medicamentos más vendidos en Sede Oeste", fuente);
        tituloChunk6.setUnderline(1f, -2f);  // grosor y desplazamiento del subrayado

        // Crear un Paragraph y añadir el Chunk
        Paragraph titulo6 = new Paragraph(tituloChunk6);
        titulo6.setAlignment(Paragraph.ALIGN_CENTER);

        documento.add(titulo6);

        documento.add(espacio);

        PdfPTable tabla6 = new PdfPTable(6);
        tabla6.setWidthPercentage(100);
        tabla6.setSpacingBefore(15);
        tabla6.setWidths(new float[] {3f, 3f, 2f, 1.8f,1.5f,1.7f});
        tabla6.setWidthPercentage(110);

        escribirCabeceraDeLaTabla(tabla6);
        escribirDatosDeLaTablaSede5(tabla6);

        documento.add(tabla6);

        documento.add(espacio);

        agregarFilaTotal(documento, "Ganancia total", String.valueOf(gananciaTotalsede5));

        documento.close();

    }













}
