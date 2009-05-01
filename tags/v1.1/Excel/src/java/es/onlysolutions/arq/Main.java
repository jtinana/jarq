package es.onlysolutions.arq;

import es.onlysolutions.arq.excel.ExcelCell;
import es.onlysolutions.arq.excel.ExcelRow;
import es.onlysolutions.arq.excel.ExcelSheet;
import es.onlysolutions.arq.excel.ExcelWorkbook;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class Main
{
    public static void main(String[] args) throws IOException
    {

//        generateExcelFromCode();

//        generateExcelFromFile();

//        probarCargaFicheros();

//        pruebaTotalizarCabecera();

//        pruebaCargarExcel();

//        String celdaValue = "17 Seur Gerona - 28 Madrid - 42 otro";
//        char[] separadores = new char[]{'-'};
//
//        System.out.println( ExcelWorkbook.tratarCeldaConNumeros( celdaValue, separadores ) );

        //Probar formato condicional en Excel.

        POIFSFileSystem fileSystem = new POIFSFileSystem( new FileInputStream("d:\\basura\\formatoCelda.xls") );
        HSSFWorkbook workbook = new HSSFWorkbook( fileSystem );
        HSSFSheet sheet = workbook.getSheetAt(0);
        HSSFRow row = sheet.getRow(0);
        HSSFCell cell1 = row.getCell((short) 0);
        System.out.println(
                "cell1 -> Valor: " + cell1.getNumericCellValue() + "; FillBgColor:" +
                cell1.getCellStyle().getFillBackgroundColor() + "; CellType:" +
                cell1.getCellType() + "; FgColor:" + cell1.getCellStyle().getFillForegroundColor()
                + "; " + cell1.getCellStyle().getFontIndex()
        );
        
        HSSFCell cell2 = row.getCell((short) 1);
        System.out.println(
                "cell2 -> Valor: " + cell2.getNumericCellValue() + "; FillBgColor:" +
                cell2.getCellStyle().getFillBackgroundColor() + "; CellType:" +
                cell2.getCellType() + "; FgColor:" + cell2.getCellStyle().getFillForegroundColor()
                + "; " + cell2.getCellStyle().getFontIndex()
        );
    }

    private static void generateExcelFromFile()
    {
        ExcelWorkbook workbook = new ExcelWorkbook();
        workbook.loadExcelFile("c:\\excelIn.xls");

        workbook.writeExcel("c:\\excelOut.xls");
    }

    private static void generateExcelFromCode()
    {
        ExcelSheet matrix = new ExcelSheet("Hola-");
        matrix.setColumnWidth(0, 8000);
        ExcelWorkbook excelWorkbook = new ExcelWorkbook();

        ExcelRow cabecera = new ExcelRow(5);

        ExcelCell celdaBlanco = new ExcelCell();
        celdaBlanco.setId("Celda en blanco");
        celdaBlanco.setContentValue("Celda en blanco");
        celdaBlanco.setRegionHeight(3);
        celdaBlanco.setRegionWidth((short) 1);

        ExcelCell actualN = new ExcelCell();
        actualN.setId("ACTUAL N");
        actualN.setContentValue("ACTUAL N");
        actualN.setRegionWidth((short) 2);
        actualN.setRegionHeight(1);
        actualN.setStyle(getTitleStyle(excelWorkbook));

        ExcelCell kE = new ExcelCell();
        kE.setId("K E");
        kE.setContentValue("K E");

        ExcelCell vsSales = new ExcelCell();
        vsSales.setId("vs / sales");
        vsSales.setContentValue("% \n vs/sales");
        vsSales.setRegionHeight(2);
        vsSales.setRegionWidth((short) 1);

        cabecera.addCell(0, celdaBlanco);
        cabecera.addCell(1, actualN);

        ExcelRow segundaLinea = new ExcelRow(2);
        segundaLinea.addCell(1, kE);
        segundaLinea.addCell(2, vsSales);

        ExcelRow terceraLinea = new ExcelRow(5);

        ExcelCell netDomesticSales = new ExcelCell();
        netDomesticSales.setContentValue("NET DOMESTIC SALES");
        netDomesticSales.setId("NET_DOMESTIC_SALES");

        ExcelCell netDomesticSalesK = new ExcelCell();
        netDomesticSalesK.setContentValue(10);
        netDomesticSalesK.setId("NET_DOMESTIC_SALES_K_VALUE");

        ExcelCell netDomesticSalesFormula = new ExcelCell();
        netDomesticSalesFormula.setContentValue("");
        netDomesticSalesFormula.setId("NET DOMESTIC SALES FORMULA");
        netDomesticSalesFormula.setFormula("#NET_DOMESTIC_SALES_K_VALUE#*3");

        terceraLinea.addCell(0, netDomesticSales);
        terceraLinea.addCell(1, netDomesticSalesK);
        terceraLinea.addCell(2, netDomesticSalesFormula);

        matrix.addRow(cabecera);
        matrix.addRow(segundaLinea);
        matrix.addRow(new ExcelRow());
        matrix.addRow(new ExcelRow());
        matrix.addRow(new ExcelRow());
        matrix.addRow(new ExcelRow());
        matrix.addRow(terceraLinea);

        excelWorkbook.addSheet(matrix);

        excelWorkbook.writeExcel("c:\\excel.xls");
    }

    private static void pruebaTotalizar()
    {
        ExcelWorkbook w1 = new ExcelWorkbook();
        ExcelWorkbook w2 = new ExcelWorkbook();

        ExcelSheet sheet1 = new ExcelSheet("Hoja1");
        ExcelSheet sheet2 = new ExcelSheet("Hoja1");

        ExcelRow row = new ExcelRow(5);

        ExcelCell cell = new ExcelCell();
        cell.setId("blank");
        cell.setContentValue(3);

        row.addCell(5, cell);

        sheet1.addRow(row);

        ExcelRow row2 = new ExcelRow(5);
        ExcelCell cell2 = new ExcelCell();
        cell2.setId("blank2");
        cell2.setContentValue(2);
        row2.addCell(5, cell2);

        sheet2.addRow(row2);

        w1.addSheet(sheet1);
        w2.addSheet(sheet2);

        w1.writeExcel("d:\\basura\\totalizaciones\\w1.xls");
        w2.writeExcel("d:\\basura\\totalizaciones\\w2.xls");

        w1.totalize(w2);
        w1.totalize(w2);
        w1.writeExcel("d:\\basura\\totalizaciones\\resultado.xls");


    }

    private static HSSFCellStyle getTitleStyle(ExcelWorkbook ee)
    {


        HSSFFont fTitle = ee.createFont();
        fTitle.setFontName(HSSFFont.FONT_ARIAL);
        fTitle.setFontHeightInPoints((short) 8);
        fTitle.setColor(HSSFColor.WHITE.index);
        fTitle.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);

        HSSFCellStyle titleStyle = ee.createCellStyle();
        titleStyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);
        titleStyle.setLeftBorderColor(HSSFColor.WHITE.index);
        titleStyle.setBorderTop(HSSFCellStyle.BORDER_THIN);
        titleStyle.setTopBorderColor(HSSFColor.WHITE.index);
        titleStyle.setBorderRight(HSSFCellStyle.BORDER_THIN);
        titleStyle.setRightBorderColor(HSSFColor.WHITE.index);
        titleStyle.setBorderBottom(HSSFCellStyle.BORDER_THIN);
        titleStyle.setBottomBorderColor(HSSFColor.WHITE.index);
        titleStyle.setFillForegroundColor(HSSFColor.DARK_BLUE.index);
        titleStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
        titleStyle.setFont(fTitle);
        titleStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);

        return titleStyle;
    }

    private static void probarCargaFicheros() throws FileNotFoundException
    {
        ExcelWorkbook w = new ExcelWorkbook();
        FileInputStream in = new FileInputStream("D:\\Proyectos\\SEUR_I_HEAD\\SEUR_I\\public_html\\WEB-INF\\classes\\informes\\Res_FC_Mv.xls");
        w.loadExcel(in);
        w.setCellValue(0, "B", 8, "Hola");
        w.writeExcel("d:\\basura\\salida.xls");
    }

    private static void pruebaTotalizarCabecera()
    {
        //Primer workbook
        ExcelWorkbook w1 = new ExcelWorkbook();
        ExcelSheet s1 = new ExcelSheet("Sheet1");
        ExcelRow r1 = new ExcelRow();

        for (int i = 0; i < 10; i++)
        {
            ExcelCell cell = new ExcelCell(String.valueOf(i));
            cell.setContentValue(i);
            r1.addCell(cell);
        }

        ExcelCell celdaObjectivo = new ExcelCell("CELDA_OBJETIVO");
        celdaObjectivo.setContentValue("HOLA");

        r1.addCell(celdaObjectivo);
        s1.addRow(r1);
        w1.addSheet(s1);

        //Segundo workbook
        ExcelWorkbook w2 = new ExcelWorkbook();
        ExcelSheet s2 = new ExcelSheet("Sheet1");
        ExcelRow r2 = new ExcelRow();

        for (int i = 0; i < 10; i++)
        {
            ExcelCell cell = new ExcelCell(String.valueOf(i));
            cell.setContentValue(i);
            r2.addCell(cell);
        }

        ExcelCell celdaObjectivo2 = new ExcelCell("CELDA_OBJETIVO");
        celdaObjectivo2.setContentValue("ADIOS");

        r2.addCell(celdaObjectivo2);
        s2.addRow(r2);
        w2.addSheet(s2);

        w1.totalize(w2, "CELDA_OBJETIVO");

        w1.writeExcel("d:\\basura\\totalizaciones\\resultado.xls");

    }

    private static void pruebaCargarExcel() throws FileNotFoundException
    {
        ExcelWorkbook w = new ExcelWorkbook();

        FileInputStream in = new FileInputStream("D:\\temp\\Res_FC_Mv_Tarifa.xls");

        w.loadExcel(in);

        w.writeExcel("d:\\temp\\salida_fc.xls");
    }

    private static void lecturaEstilos() throws IOException
    {
        FileInputStream in = new FileInputStream("c:\\basura\\excel.xls");
        POIFSFileSystem fileSystem = new POIFSFileSystem(in);
        HSSFWorkbook w = new HSSFWorkbook(fileSystem);

        HSSFSheet sheet = w.getSheetAt(0);
        for (int index = 0; index < sheet.getPhysicalNumberOfRows(); index++)
        {
            HSSFRow row = sheet.getRow(index);
            for (int indexRow = 0; indexRow < row.getPhysicalNumberOfCells(); indexRow++)
            {
                HSSFCell cell = row.getCell((short) indexRow);
                cell.getCellComment().getString().getString();
                String cellText = cell.getRichStringCellValue().getString();
                if("Titulo General".equals(cellText))
                {
                    HSSFCellStyle style = cell.getCellStyle();

                }
            }

        }
    }

}
