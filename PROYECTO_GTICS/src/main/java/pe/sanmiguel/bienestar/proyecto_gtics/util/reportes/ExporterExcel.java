package pe.sanmiguel.bienestar.proyecto_gtics.util.reportes;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;
import pe.sanmiguel.bienestar.proyecto_gtics.Dto.OrdenesExporterDto;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;

public class ExporterExcel {

    private XSSFWorkbook libro;
    private XSSFSheet hoja;

    private List<OrdenesExporterDto> listaOrdenesExportar;

    public ExporterExcel(List<OrdenesExporterDto> listaOrdenesExportar) {
        this.listaOrdenesExportar = listaOrdenesExportar;
        libro = new XSSFWorkbook();
        hoja = libro.createSheet("Ordenes Web");
    }

    private void escribirCabeceraTabla(){
        Row fila = hoja.createRow(0);

        XSSFCellStyle estilo = (XSSFCellStyle) libro.createCellStyle();

        XSSFFont fuente = libro.createFont();
        fuente.setBold(true);
        fuente.setFontHeight(16);
        estilo.setFont(fuente);

        // Configurar la alineación horizontal y vertical centrada
        estilo.setAlignment(HorizontalAlignment.CENTER);
        estilo.setVerticalAlignment(VerticalAlignment.CENTER);

        // Configurar los bordes de las celdas
        estilo.setBorderTop(BorderStyle.THIN);
        estilo.setBorderBottom(BorderStyle.THIN);
        estilo.setBorderLeft(BorderStyle.THIN);
        estilo.setBorderRight(BorderStyle.THIN);

        // Establecer el color de fondo usando un valor hexadecimal
        XSSFColor colorAzul = new XSSFColor(java.awt.Color.decode("#1f73ef"), new DefaultIndexedColorMap());
        estilo.setFillForegroundColor(colorAzul);
        estilo.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // Establecer el color de la fuente en blanco para contraste
        XSSFColor colorBlanco = new XSSFColor(java.awt.Color.decode("#FFFFFF"), new DefaultIndexedColorMap());
        fuente.setColor(colorBlanco);

        Cell celda = fila.createCell(0);
        celda.setCellValue("Nombres del paciente");
        celda.setCellStyle(estilo);

        celda = fila.createCell(1);
        celda.setCellValue("Apellidos del paciente");
        celda.setCellStyle(estilo);

        celda = fila.createCell(2);
        celda.setCellValue("Fecha de inicio");
        celda.setCellStyle(estilo);

        celda = fila.createCell(3);
        celda.setCellValue("Fecha de fin");
        celda.setCellStyle(estilo);

        celda = fila.createCell(4);
        celda.setCellValue("Sede");
        celda.setCellStyle(estilo);

        celda = fila.createCell(5);
        celda.setCellValue("Precio total (S/.)");
        celda.setCellStyle(estilo);
    }

    private void escribirDatosTabla(){

        int numeroFilas = 1;

        CellStyle estilo = libro.createCellStyle();
        XSSFFont fuente = libro.createFont();
        fuente.setFontHeight(14);
        estilo.setFont(fuente);

        // Configurar la alineación horizontal y vertical centrada
        estilo.setAlignment(HorizontalAlignment.CENTER);
        estilo.setVerticalAlignment(VerticalAlignment.CENTER);

        // Configurar los bordes de las celdas
        estilo.setBorderTop(BorderStyle.THIN);
        estilo.setBorderBottom(BorderStyle.THIN);
        estilo.setBorderLeft(BorderStyle.THIN);
        estilo.setBorderRight(BorderStyle.THIN);

        //Decimal Format
        DecimalFormat df = new DecimalFormat("#.##");

        for(OrdenesExporterDto ordenesExporterDto : listaOrdenesExportar){
            Row fila = hoja.createRow(numeroFilas++);

            Cell celda = fila.createCell(0);
            celda.setCellValue(ordenesExporterDto.getNombres());
            hoja.autoSizeColumn(0);
            celda.setCellStyle(estilo);

            celda = fila.createCell(1);
            celda.setCellValue(ordenesExporterDto.getApellidos());
            hoja.autoSizeColumn(1);
            celda.setCellStyle(estilo);

            celda = fila.createCell(2);
            celda.setCellValue(ordenesExporterDto.getFechaIni().toString());
            hoja.autoSizeColumn(2);
            celda.setCellStyle(estilo);

            celda = fila.createCell(3);
            celda.setCellValue(ordenesExporterDto.getFechaFin().toString());
            hoja.autoSizeColumn(3);
            celda.setCellStyle(estilo);

            celda = fila.createCell(4);
            celda.setCellValue(ordenesExporterDto.getNombreSede());
            hoja.autoSizeColumn(4);
            celda.setCellStyle(estilo);

            celda = fila.createCell(5);
            double precioTotal = ordenesExporterDto.getPrecioTotal();
            celda.setCellValue(Double.parseDouble(df.format(precioTotal)));
            hoja.autoSizeColumn(5);
            celda.setCellStyle(estilo);

        }

    }

    public void exportar(HttpServletResponse response) throws IOException {
        escribirCabeceraTabla();
        escribirDatosTabla();

        ServletOutputStream outputStream = response.getOutputStream();
        libro.write(outputStream);

        libro.close();
        outputStream.close();
    }



}
