package es.onlysolutions.arq.excel;

import es.onlysolutions.arq.core.log.LoggerGenerator;
import es.onlysolutions.arq.excel.exception.ExcelEngineException;
import org.apache.commons.logging.Log;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.hssf.usermodel.HSSFPrintSetup;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.CellReference;
import org.apache.poi.hssf.util.Region;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.springframework.util.Assert;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Motor para la generacion de excel a partir de matrices de celdas.
 */
public class ExcelWorkbook implements Serializable, Cloneable
{
    /**
     * El logger de la clase.
     */
    private static final Log logger = LoggerGenerator.getLogger(ExcelWorkbook.class);

    /**
     * El woorkbook a partir del cual se generara este report.
     */
    private HSSFWorkbook workbook = new HSSFWorkbook();

    /**
     * El nombre del Workbook.
     */
    private String workbookName;

    /**
     * Las hojas internas de este woorkbook.
     */
    private List<ExcelSheet> internalSheets = new ArrayList<ExcelSheet>(3);

    /**
     * Flag para indicar cuando el ExcelWorkbook ha sido generado leyendo de fichero, y por lo tanto es necesario limpiar
     * la referencia interna al POI antes de generarlo de nuevo.
     */
    private Boolean isReadedFromFile = false;

    /**
     * Codigo de operacion para totalizar el ExcelWorkbook. Se realiza una suma algebraica, de la ExcelWorkbook actual
     * y la destino, con los valores de las celdas que sean numericas , no realizandose accion alguna sobre el resto de celdas.
     */
    private Integer TOTALIZE = 1;

    /**
     * Lista de print areas a a�adir al woorkbook.
     */
    private List<PrintArea> printAreas = new ArrayList<PrintArea>(5);

    /**
     * Codigo de operacion para realizar el comparativo con un ExcelWorkbook. Se realiza una resta entre los valores de las
     * celdas de la ExcelWorkbook actual menos los valores de las celdas de la destino. No se realiza accion alguna sobre
     * las celdas que no sean numericas.
     */
    private Integer COMPARE = 2;

    /**
     * Array de caracteres que se utilizaran como separadores para tratar el contenido de las celdas.
     */
    private static final char[] separators = new char[]{'-'};

    /**
     * Constructor sin parametros.
     */
    public ExcelWorkbook()
    {
        super();
    }

    /**
     * Constructor indicando el nombre del Workbook.
     * @param name El nombre del workbook.
     */
    public ExcelWorkbook( String name )
    {
        super();
        this.workbookName = name;
    }

    /**
     * Contruye un workbook a partir de un objeto HSSFWorkbook de POI.
     * Se permite esta construccion para facilitar su integracion con sistemas antiguos,
     * pero esta deprecado y deberia evitarse en su mayor medida.
     *
     * @param workbook El objeto HSSFWorkbook con el que crear el objeto.
     * @see org.apache.poi.hssf.usermodel.HSSFCellStyle
     * @deprecated Utilizarlo solo para compatibilidad con codigo antiguo.
     */
    public ExcelWorkbook(HSSFWorkbook workbook)
    {
        this.workbook = workbook;
    }

    /**
     * Agrega una hoja a este Workbook.
     *
     * @param theSheet La hoja de excel a agregar.
     */
    public void addSheet(ExcelSheet theSheet)
    {
        this.internalSheets.add(theSheet);
    }

    /**
     * Asigna la pesta�a cuyos datos se muestran cuando el libro se abre.
     *
     * @param index Posicion de la pesta�a seleccionada.
     */
    public void setSelectedTab(short index)
    {
        this.workbook.setSelectedTab(index);
    }

    /**
     * Crea una nuevo tipo de fuente y lo asocia a este motor de excel.
     *
     * @return El nuevo tipo de font.
     */
    public HSSFFont createFont()
    {
        return this.workbook.createFont();
    }

    /**
     * Crea un nuevo estilo y lo asocia a este motor de excel.
     *
     * @return El nuevo estilo.
     * @see org.apache.poi.hssf.usermodel.HSSFCellStyle
     */
    public HSSFCellStyle createCellStyle()
    {
        return this.workbook.createCellStyle();
    }

    /**
     * Escribe en la ruta indicada la excel generada. Si el fichero ya existe lo sobreescribe.
     *
     * @param pathToFile La ruta del fichero a crear. Debe ser un fichero valido.
     */
    public void writeExcel(String pathToFile)
    {

        HSSFWorkbook hssfWorkbook;

        if (isReadedFromFile)
        {
            hssfWorkbook = this.workbook;
        }
        else
        {
            hssfWorkbook = generateExcel();
        }
        FileOutputStream fos = null;
        try
        {
            fos = new FileOutputStream(pathToFile);
            hssfWorkbook.write(fos);
        }
        catch (FileNotFoundException e)
        {
            logger.error(e);
            throw new ExcelEngineException("No se ha podido crear el fichero: " + pathToFile, e);
        }
        catch (IOException e)
        {
            logger.error(e);
            throw new ExcelEngineException("No se ha podido escribir sobre el fichero: " + pathToFile, e);
        }
        finally
        {
            if (fos != null)
            {
                try
                {
                    fos.close();
                }
                catch (IOException e)
                {
                    logger.error(e);
                }
            }
        }

    }

    /**
     * General la hoja excel completa a partir de las hojas que hayan sido a�adidas previamente.
     *
     * @return El objeto excel.
     * @see es.onlysolutions.arq.excel.ExcelSheet
     * @see es.onlysolutions.arq.excel.ExcelCell
     */
    public HSSFWorkbook generateExcel()
    {
        if( !isReadedFromFile )
        {
            Assert.notEmpty(this.internalSheets, "Debe agregar previamente alguna Hoja al ExcelWorkbook");
        }

        //A�adimos los print areas que existan.
        for (int indexPrintArea = 0; indexPrintArea < this.printAreas.size(); indexPrintArea++)
        {
            PrintArea pa = this.printAreas.get(indexPrintArea);
            this.workbook.setPrintArea(pa.getSheetIndex(), pa.getStartColumn(), pa.getEndColumn(), pa.getStartRow(), pa.getEndRow());
        }

        //Recorremos las matrices que haya.
        for (int sheetIndex = 0; sheetIndex < internalSheets.size(); sheetIndex++)
        {
            //La hoja con la que trabajar.
            ExcelSheet excelSheet = internalSheets.get(sheetIndex);
            excelSheet.establishCellPositions();

            //Creamos una hoja para esa matriz, o si se ha leido de fichero, utilizamos la existente.
            HSSFSheet sheet;

            if (isReadedFromFile)
            {
                sheet = this.workbook.getSheetAt(sheetIndex);

                if (logger.isDebugEnabled())
                {
                    logger.debug("Recuperada de fichero HSSFSheet con nombre: " + excelSheet.getName() + " y objeto: " + sheet);
                }
            }
            else
            {
                if( this.workbook.getNumberOfSheets() > sheetIndex )
                {
                    this.workbook.removeSheetAt( sheetIndex );
                }
                sheet = workbook.createSheet(getCorrectSheetName(this.workbook, excelSheet));

                if (logger.isDebugEnabled())
                {
                    logger.debug("Creada HSSFSheet con nombre: " + excelSheet.getName() + " y objeto: " + sheet);
                }
            }


            if (excelSheet.getFitToPage() != null)
            {
                sheet.setFitToPage(excelSheet.getFitToPage());
            }
            else
            {
                sheet.setFitToPage(true);
            }

            if (excelSheet.getVerticallyCenter() != null)
            {
                sheet.setVerticallyCenter(excelSheet.getVerticallyCenter());
            }

            if (excelSheet.getHorizontallyCenter() != null)
            {
                sheet.setHorizontallyCenter(excelSheet.getHorizontallyCenter());
            }

            if (excelSheet.getColumnBreak() != null)
            {
                sheet.setColumnBreak(excelSheet.getColumnBreak());
            }

            if (excelSheet.getRowBreak() != null)
            {
                sheet.setRowBreak(excelSheet.getRowBreak());
            }

            if (excelSheet.getAutoBreak() != null)
            {
                sheet.setAutobreaks(excelSheet.getAutoBreak());
            }

            if (excelSheet.getPrintSetup() != null)
            {
                HSSFPrintSetup printSetup = sheet.getPrintSetup();
                copyPrintSetup(printSetup, excelSheet.getPrintSetup());
            }

            //Recorremos las filas de la matriz.

            List<ExcelRow> rows = excelSheet.getRowList();
            Iterator<ExcelRow> rowIterator = rows.iterator();

            int rowIndex = 0;
            while (rowIterator.hasNext())
            {
                ExcelRow matrixRow = rowIterator.next();
                Iterator<ExcelCell> cellIterator = matrixRow.iterator();

                //Si se lee de fichero los rows ya tienen datos, asi que se utilizan los que ya estan.
                HSSFRow row;

                if (isReadedFromFile)
                {
                    row = sheet.getRow(rowIndex);
                }
                else
                {
                    row = sheet.createRow(rowIndex);
                }

                if (matrixRow.getHeightInPoints() != null)
                {
                    row.setHeightInPoints(matrixRow.getHeightInPoints());
                }

                int cellIndex = 0;
                while (cellIterator.hasNext())
                {
                    ExcelCell newExcelCell = cellIterator.next();

                    if (newExcelCell != null)
                    {

                        //Si se lee de fichero, se utiliza la celda que ya existe, y si no se crea una nueva.
                        HSSFCell innerCell = null;

                        if (isReadedFromFile)
                        {
                            if (row != null)
                            {
                                innerCell = row.getCell((short) cellIndex);
                            }
                        }
                        else
                        {
                            if (row != null)
                            {
                                innerCell = row.createCell((short) cellIndex);
                            }
                        }

                        if (row != null)
                        {
                            copyValuesToHSSFCell(innerCell, newExcelCell, row, (short) cellIndex);
                        }
                        resolveFormulaNames(excelSheet, innerCell, newExcelCell);

                        //Creamos la region si procede
                        if (newExcelCell.getRegionWidth() != null && newExcelCell.getRegionWidth() > 0 && newExcelCell.getRegionHeight() != null && newExcelCell.getRegionHeight() > 0)
                        {
                            int rowFrom = rowIndex;
                            int rowTo = rowIndex + newExcelCell.getRegionHeight() - 1;
                            short cellFrom = (short) cellIndex;
                            short cellTo = (short) (cellIndex + newExcelCell.getRegionWidth() - 1);
                            Region region = new Region(rowFrom, cellFrom, rowTo, cellTo);
                            sheet.addMergedRegion(region);

                            //Tenemos que avanzar el indice de celdas en funcion del tama�o de la region para que el resto de celdas cuadren.
                            cellIndex += newExcelCell.getRegionWidth() - 1;

                            if (logger.isDebugEnabled())
                            {
                                logger.debug("Creada region [rowFrom: " + rowFrom + " , rowTo: " + rowTo + " , cellFrom: " + cellFrom + " , cellTo: " + cellTo + " ]");
                            }
                        }
                    }
                    cellIndex++;
                }
                rowIndex++;
            }

            //Establecemos el ancho de las columnas.
            setColumnsWidths(sheet, excelSheet);


        }

        return workbook;

    }

    /**
     * Obtiene un nombre valido para el nuevo Sheet. En caso de existir uno con el mismo nombre se introduce entre parentesis
     * un numero, que sera (1) en caso de ser la primera repeticion, o un numero consecutido al existente.
     *
     * @param hssfWorkbook El workbook donde del que se va a crear el nuevo sheet.
     * @param excelSheet   El ExcelSheet del que se quiere obtener el nombre.
     * @return El nombre valido de Sheet para que no existan conflictos.
     */
    private String getCorrectSheetName(HSSFWorkbook hssfWorkbook, ExcelSheet excelSheet)
    {
        //Nombre final sin conflictos.
        String resultSheetName;

        String newSheetName = excelSheet.getName();
        HSSFSheet oldHssfSheet = hssfWorkbook.getSheet(newSheetName);

        if (oldHssfSheet == null)
        {
            resultSheetName = newSheetName;
        }
        else //Significa que existe ya una hoja con ese nombre.
        {
            int indexOfParentesis = newSheetName.indexOf('(');

            if (indexOfParentesis == -1) //Es la primera repeticion.
            {
                resultSheetName = newSheetName + "(1)";
            }
            else
            {
                String numRepeticion = newSheetName.substring(indexOfParentesis, indexOfParentesis + 2);
                Integer rep;
                try
                {
                    rep = Integer.valueOf(numRepeticion) + 1;
                    resultSheetName = newSheetName.substring(0, indexOfParentesis) + "(" + rep + ")";
                }
                catch (NumberFormatException e)
                {
                    logger.warn("La hoja contenia entre parentesis el valor: " + numRepeticion + ". Se inicia la cuenta de nuevo.");
                    resultSheetName = newSheetName + "(1)";
                }

            }
        }

        return resultSheetName;
    }

    /**
     * Copia las propiedades del ExcelPrintSetup al HSSFPrintSetup destino.
     *
     * @param hssfPrintSetup  El HSSFPrintSetup.
     * @param excelPrintSetup El ExcelPrintSetup;
     */
    private void copyPrintSetup(HSSFPrintSetup hssfPrintSetup, ExcelPrintSetup excelPrintSetup)
    {
        if (excelPrintSetup.getLandScape() != null)
        {
            hssfPrintSetup.setLandscape(excelPrintSetup.getLandScape());
        }

        if (excelPrintSetup.getLefToRight() != null)
        {
            hssfPrintSetup.setLeftToRight(excelPrintSetup.getLefToRight());
        }

        if (excelPrintSetup.getFitHeight() != null)
        {
            hssfPrintSetup.setFitHeight(excelPrintSetup.getFitHeight());
        }

        if (excelPrintSetup.getFitWidth() != null)
        {
            hssfPrintSetup.setFitWidth(excelPrintSetup.getFitWidth());
        }
    }

    /**
     * Establece en el HSSFSheet el ancho por defecto indicado en el ExcelSheet. Si se indico a null se trata de calcular.
     *
     * @param targetSheet El sheet donde establecer el ancho.
     * @param excelSheet  El objeto ExcelSheet del que obtener el ancho por defecto.
     */
    private void setColumnsWidths(HSSFSheet targetSheet, ExcelSheet excelSheet)
    {
        Map<Short, Short> columnWidths = excelSheet.getColumnWidths();

        Iterator<Map.Entry<Short, Short>> itColumns = columnWidths.entrySet().iterator();
        while (itColumns.hasNext())
        {
            Map.Entry<Short, Short> entry = itColumns.next();
            Short columnIndex = entry.getKey();
            Short columnWidht = entry.getValue();
            targetSheet.setColumnWidth(columnIndex, columnWidht);
        }

        Short defaultColumnWidth = excelSheet.getDefaultColumnWidth();
        if (defaultColumnWidth != null)
        {
            targetSheet.setDefaultColumnWidth(defaultColumnWidth);
        }
    }

    /**
     * Resuelve los id indicados en la formula y los sustituye por el valor real de la celda en excel.
     * Supone que todos los id estaran separados por el caracter indicado en la constante ID_SEPARATOR
     * de la clase ExcelSheet.
     *
     * @param matrix        La matriz.
     * @param targetCell    La celda donde establecer la formula final.
     * @param origExcelCell La celda de la que resolver los nombres de la formula.
     */
    private void resolveFormulaNames(ExcelSheet matrix, HSSFCell targetCell, ExcelCell origExcelCell)
    {
        if (targetCell != null)
        {

            String idsFormula = origExcelCell.getFormula();

            if ( idsFormula != null && idsFormula.length() > 0 )
            {
                /**
                 * Tratamos la formula y si existen un divisor, introducimos sentencia IF para evitar errores de division por cero.
                 */

                String functionFormula;

                if( idsFormula.indexOf('/') != -1 ) //Lo contiene.
                {
                    functionFormula = "IF(ISERROR(" + idsFormula + ")=TRUE;0;" + idsFormula + ')';
                }
                else
                {
                    functionFormula = idsFormula;
                }

                StringBuffer sbIdFormula = new StringBuffer(functionFormula);

                while (sbIdFormula.indexOf(ExcelSheet.ID_SEPARATOR) != -1)
                {
                    //Quedan ids por reemplazar.
                    int indexIdFrom = sbIdFormula.indexOf(ExcelSheet.ID_SEPARATOR);
                    int indexIdTo = sbIdFormula.indexOf(ExcelSheet.ID_SEPARATOR, indexIdFrom + 1);

                    if (indexIdTo == -1)
                    {
                        throw new IllegalArgumentException("Ha abierto un caracter de separacion de ID pero no lo ha cerrado. Revise la formula de la celda: " + origExcelCell);
                    }

                    String idToReplace = sbIdFormula.substring(indexIdFrom, indexIdTo + 1);

                    String strippedId = stripId(idToReplace);
                    ExcelPosition idPosition = matrix.getExcelPositionById(strippedId);

                    if (idPosition == null)
                    {
                        sbIdFormula.replace(indexIdFrom, indexIdTo + 1, "0");
                        logger.warn("El ID: " + strippedId + " no existe, y se ha reemplazado por un 0");
                    }
                    else
                    {
                        sbIdFormula.replace(indexIdFrom, indexIdTo + 1, idPosition.toString());
                    }
                }

                targetCell.setCellFormula(sbIdFormula.toString());

                if (logger.isDebugEnabled())
                {
                    logger.debug("Formula original : " + idsFormula);
                    logger.debug("Formula reemplazada : " + sbIdFormula.toString());
                }
            }
        }
    }

    /**
     * Elimina del ID los tokens de inicio y fin.
     *
     * @param idToReplace El identificador a tratar.
     * @return El identificador sin sus separadores.
     * @see es.onlysolutions.arq.excel.ExcelSheet#ID_SEPARATOR
     */
    private String stripId(String idToReplace)
    {
        return idToReplace.replaceAll(ExcelSheet.ID_SEPARATOR, "");
    }

    /**
     * Copia todos los valores desde la celda de la matriz a la celda de POI.
     * Realiza un relleno adecuado de la celda de POI en base a los valores establecidos en la celda de la matriz.
     *
     * @param targetCell    La celda donde se guardaran los valores.
     * @param origExcelCell La celda desde la que obtener los valores a copiar.
     * @param row           El HSSFRow del que se ha obtenido la celda.
     * @param cellIndex     El indice de la columna donde estamos.
     */
    private void copyValuesToHSSFCell(HSSFCell targetCell, ExcelCell origExcelCell, HSSFRow row, short cellIndex)
    {
        if (targetCell != null)
        {
            if ( origExcelCell != null && !ExcelRow.UNIQUE_ID.equals(origExcelCell.getId()))
            {
                setContent(origExcelCell, targetCell);

                if (origExcelCell.getStyle() != null)
                {
                    targetCell.setCellStyle(origExcelCell.getStyle());
                }

                if (origExcelCell.getEncoding() != null)
                {
                    targetCell.setEncoding(origExcelCell.getEncoding());
                }
        }
        }
        else if (origExcelCell != null)
        {
            targetCell = row.createCell(cellIndex);
        }
    }

    /**
     * Introduce el contenido en la celda objetivo.
     *
     * @param targetCell  La celda destino en la que establecer el contenido.
     * @param cellContent El valor a establecer en la celda origen.
     */
    private void setContent(HSSFCell targetCell, Object cellContent)
    {
        if (targetCell != null && targetCell.getCellType() != HSSFCell.CELL_TYPE_FORMULA && targetCell.getCellType() != HSSFCell.CELL_TYPE_ERROR)
        {

            if (cellContent != null)
            {
                if (cellContent instanceof Calendar)
                {
                    targetCell.setCellValue((Calendar) cellContent);
                    targetCell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
                }
                else if (cellContent instanceof Date)
                {
                    targetCell.setCellValue((Date) cellContent);
                    targetCell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
                }
                else if (cellContent instanceof String)
                {
                    HSSFRichTextString richTextString = new HSSFRichTextString((String) cellContent);
                    targetCell.setCellValue(richTextString);
                    targetCell.setCellType(HSSFCell.CELL_TYPE_STRING);
                }
                else if (cellContent instanceof Double)
                {
                    targetCell.setCellValue((Double) cellContent);
                    targetCell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
                }
                else if (cellContent instanceof Boolean)
                {
                    targetCell.setCellValue((Boolean) cellContent);
                    targetCell.setCellType(HSSFCell.CELL_TYPE_BOOLEAN);
                }
                else if (cellContent instanceof Integer)
                {
                    targetCell.setCellValue(((Integer) cellContent).doubleValue());
                    targetCell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
                }
                else if (cellContent instanceof Float)
                {
                    targetCell.setCellValue(((Float) cellContent).doubleValue());
                    targetCell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
                }
                else if (cellContent instanceof Long)
                {
                    targetCell.setCellValue(((Long) cellContent).doubleValue());
                    targetCell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
                }
                else
                {
                    throw new IllegalArgumentException("El tipo " + cellContent.getClass().getName() + " no esta soportado como contenido de las celdas");
                }
            }
        }
    }

    /**
     * Introduce el contenido en la celda objetivo.
     *
     * @param origExcelCell La celda origen de la que obtener el contenido.
     * @param targetCell    La celda destino.
     */
    private void setContent(ExcelCell origExcelCell, HSSFCell targetCell)
    {
        Object cellContent = null;

        if (origExcelCell != null)
        {
            cellContent = origExcelCell.getContentValue();
        }

        setContent(targetCell, cellContent);

    }


    /**
     * �ste metodo construye un objeto ExcelWorkbook a partir de un fichero Excel. Utiliza para ello la API de POI,
     * y encapsula todos las hojas excel leidas en objeto de la arquitectura. Delega la ejecucion en el metodo loadExcel
     * pero indicando el parametro preserveNodes a false.
     *
     * @param in El InputStream del que leer el fichero Excel.
     */
    public void loadExcel(InputStream in)
    {
        this.loadExcelFile(in, false);
    }

    public void loadExcelFile(String pathToExcelFile)
    {
        FileInputStream fileInputStream = null;
        try
        {
            fileInputStream = new FileInputStream(pathToExcelFile);
            this.loadExcel(fileInputStream);
        }
        catch (FileNotFoundException e)
        {
            logger.error(e);
            throw new ExcelEngineException("No se ha podido crear el FileOutputStream al fichero: " + pathToExcelFile, e);
        }
        finally
        {
            if (fileInputStream != null)
            {
                try
                {
                    fileInputStream.close();
                }
                catch (IOException e)
                {
                    logger.error(e);
                }
            }
        }
    }

    /**
     * �ste metodo construye un objeto ExcelWorkbook a partir de un fichero Excel. Utiliza para ello la API de POI,
     * y encapsula todos las hojas excel leidas en objeto de la arquitectura.
     *
     * @param in            El InputStream del que leer el fichero Excel.
     * @param preserveNodes Parametro que indica cuando preservar los nodos originales de la Excel. (Macros, etc).
     *                      Dado que es un procesamiento costoso utilizarlo solo si es necesario.
     */
    public void loadExcelFile(InputStream in, boolean preserveNodes)
    {
        Assert.notNull(in, "No se puede pasar un InputStream nulo");
        try
        {
            POIFSFileSystem poifsFileSystem = new POIFSFileSystem(in);
            HSSFWorkbook hssfWorkbook = new HSSFWorkbook(poifsFileSystem, preserveNodes);

            if (logger.isDebugEnabled())
            {
                logger.debug("Cargado HSSFWorkbook desde fichero. Procedemos a cargar el ExcelWorkbook con  " + hssfWorkbook.getNumberOfSheets() + " sheets.");
            }

            //Cargamos el workbook para facilitar su exportacion.
            this.workbook = hssfWorkbook;
            //Recorremos todas las hojas
            for (int sheetIndex = 0; sheetIndex < hssfWorkbook.getNumberOfSheets(); sheetIndex++)
            {
                HSSFSheet sheet = hssfWorkbook.getSheetAt(sheetIndex);
                String sheetName = hssfWorkbook.getSheetName(sheetIndex);

                ExcelSheet excelSheet = new ExcelSheet(sheetName);

                /**
                 * Inicializamos contador para obtener el maximo numero de columnas que hay,
                 * de esta forma mas tarde estableceremos el mismo ancho para todas ellas. 
                 */

                Short maxNumberOfColumns = 0;


                if (logger.isDebugEnabled())
                {
                    logger.debug("Creamos una hoja de nombre: " + sheetName);
                }

                //Recorremos las regiones.
                for (int indexRegion = 0; indexRegion < sheet.getNumMergedRegions(); indexRegion++)
                {
                    Region region = sheet.getMergedRegionAt(indexRegion);
                    excelSheet.addRegion(region);
                }

                /**
                 * Se debe recorrer la hoja mediante iterators debido a que se obtiene las columnas fisicas, no las totales.
                 */
                Iterator<HSSFRow> rowIterator = sheet.rowIterator();
                int rowIndex = 0;
                while (rowIterator.hasNext())
                {
                    HSSFRow row = rowIterator.next();
                    ExcelRow excelRow = new ExcelRow();

                    /**
                     * A�adimos las celdas fisicas.
                     */
                    Iterator<HSSFCell> cellIterator = row.cellIterator();
                    Integer cellIndex = Integer.valueOf(0);
                    while (cellIterator.hasNext())
                    {
                        HSSFCell cell = cellIterator.next();
                        ExcelCell excelCell = new ExcelCell();

                        Region regionContenida = isFirstCellOfRegion(rowIndex, cellIndex.shortValue(), excelSheet);

                        if (regionContenida != null)
                        {
                            copyValuesToExcelCell(cell, excelCell, regionContenida);
                            excelRow.addCell(excelCell);
                            excelCell.setId("[" + rowIndex + ',' + cellIndex + "]");

                            if (logger.isDebugEnabled())
                            {
                                logger.debug("Se a�ade celda que pertence a una region: " + excelCell);
                            }
                        }
                        else if (isCellOfRegion(rowIndex, cellIndex.shortValue(), excelSheet))
                        {
                            //No a�adimos nada, ya que solo se a�ade la primera.
                            if (logger.isDebugEnabled())
                            {
                                logger.debug("No se a�ade celda para el Sheet: " + sheet);
                            }
                        }
                        else //No pertenece a ninguna region.
                        {
                            copyValuesToExcelCell(cell, excelCell);
                            excelRow.addCell(excelCell);
                            excelCell.setId("[" + rowIndex + ',' + cellIndex + "]");

                            if (logger.isDebugEnabled())
                            {
                                logger.debug("Se a�ade celda que NO pertence a region: " + excelCell);
                            }
                        }

                        cellIndex++;
                    }
                    excelSheet.addRow(excelRow);
                    rowIndex++;

                    if (cellIndex.shortValue() > maxNumberOfColumns)
                    {
                        maxNumberOfColumns = cellIndex.shortValue();
                    }

                }

                /**
                 * Establecemos el maximo numero de columnas al ancho original.
                 */

                for (short index = 0; index < maxNumberOfColumns; index++)
                {
                    excelSheet.setColumnWidth(index, sheet.getColumnWidth(index));
                }


                this.addSheet(excelSheet);
            }
        }
        catch (IOException e)
        {
            logger.error(e);
            throw new ExcelEngineException("Error al tratar de abrir el fichero excel", e);
        }

        if (logger.isInfoEnabled())
        {
            logger.info("Excel cargada correctamente");
        }

        this.isReadedFromFile = true;
    }

    /**
     * Indica si las posiciones indicadas pertenecen a alguna region de la hoja.
     *
     * @param rowIndex  el indice de la fila.
     * @param cellIndex El indice de la columna.
     * @param sheet     La hoja de la que comprobar si pertenece a alguna region.
     * @return true si pertenece a alguna region.
     */
    private Boolean isCellOfRegion(Integer rowIndex, Short cellIndex, ExcelSheet sheet)
    {
        Boolean result = Boolean.FALSE;

        for (int index = 0; index < sheet.getNumberOfRegions(); index++)
        {
            Region region = sheet.getRegion(index);
            if (region.contains(rowIndex, cellIndex.shortValue()))
            {
                result = Boolean.TRUE;
            }
        }

        return result;
    }

    /**
     * Indica si es la primera celda de una region (sera la primera si es la primera superior izquierda) devolviendo la
     * Region de la que es la primera.
     *
     * @param rowIndex    El indice de la fila
     * @param columnIndex El indice de la columna.
     * @param sheet       La ExcelSheet de la que comprobar si pertenece a una Region.
     * @return La Region de la que es la primera o null si no pertenece a ninguna.
     */
    private Region isFirstCellOfRegion(Integer rowIndex, Short columnIndex, ExcelSheet sheet)
    {
        Region result = null;

        for (int index = 0; index < sheet.getNumberOfRegions(); index++)
        {
            Region region = sheet.getRegion(index);
            if (region.contains(rowIndex, columnIndex.shortValue()))
            {
                //Si el inicio de la region coincide con los indices de la celda es que es la primera.
                if (region.getRowFrom() == rowIndex && region.getColumnFrom() == columnIndex)
                {
                    result = region;
                }
            }
        }

        if (logger.isDebugEnabled())
        {
            logger.debug("Se obtiene la region: " + result + " para los indices RowIndex[" + rowIndex + "];ColumnIndex[" + columnIndex + "];ExcelSheet -> " + sheet);
        }

        return result;
    }

    /**
     * Realiza una copia exacta de la celda de POI a la celda de la arquitectura. Se utiliza al cargar ficheros externos.
     * Si la celda de POI pasada como parametro es null, no se raliza accion alguna.
     *
     * @param cell       La celda de la que obtener los datos.
     * @param targetCell La celda objetivo en la que cargar las propiedades.
     */
    private void copyValuesToExcelCell(HSSFCell cell, ExcelCell targetCell)
    {
        this.copyValuesToExcelCell(cell, targetCell, null);
    }

    /**
     * Realiza una copia exacta de la celda de POI a la celda de la arquitectura. Se utiliza al cargar ficheros externos.
     * Si la celda de POI pasada como parametro es null, no se raliza accion alguna.
     *
     * @param cell         La celda de la que obtener los datos.
     * @param targetCell   La celda objetivo en la que cargar las propiedades.
     * @param regionOfCell La region a la que dimensionar la celda. si se indica null no se realiza redimensionamiento.
     */
    private void copyValuesToExcelCell(HSSFCell cell, ExcelCell targetCell, Region regionOfCell)
    {
        if (cell != null)
        {
            //Establecemos el contenido de la celda destino, en funcion del tipo de celda.
            int cellType = cell.getCellType();
            if (cellType == HSSFCell.CELL_TYPE_BOOLEAN)
            {
                targetCell.setContentValue(cell.getBooleanCellValue());
            }
            else if (cellType == HSSFCell.CELL_TYPE_NUMERIC)
            {
                targetCell.setContentValue(cell.getNumericCellValue());
            }
            else if (cellType == HSSFCell.CELL_TYPE_FORMULA)
            {
                targetCell.setFormula(cell.getCellFormula());
            }
            else if (cellType == HSSFCell.CELL_TYPE_STRING)
            {
                targetCell.setContentValue(cell.getRichStringCellValue().getString());
            }
            else if (cellType == HSSFCell.CELL_TYPE_BLANK)
            {
                targetCell = ExcelRow.getBLANK_CELL();
            }

            //Establecemos el estilo
            HSSFCellStyle style = cell.getCellStyle();
            if (style != null)
            {
                targetCell.setStyle(style);
            }

            //Establecemos el tipo.
            targetCell.setType(cell.getCellType());

            //Establecemos la region.
            if (regionOfCell != null)
            {
                Integer regionHeight = (regionOfCell.getRowTo() - regionOfCell.getRowFrom()) + 1;
                if (regionHeight > 0)
                {
                    targetCell.setRegionHeight(regionHeight);
                }

                Integer regionWidth = (regionOfCell.getColumnTo() - regionOfCell.getColumnFrom()) + 1;
                if (regionWidth > 0)
                {
                    targetCell.setRegionWidth(regionWidth.shortValue());
                }
            }
        }
    }

    /**
     * Realiza la suma de los campos del ExcelWorkbook actual con el pasado como parametro.
     * Se recomienda realizar la totalizacion a partir de una ExcelWorkBook clonada.
     *
     * @param workbookToTotalize El ExcelWorkbook a a�adir al actual.
     * @see #totalize(ExcelWorkbook,String)
     */
    public void totalize(ExcelWorkbook workbookToTotalize)
    {
        operateValuesWorkbook(workbookToTotalize, TOTALIZE, null);
    }

    /**
     * Realiza la suma de los campos del ExcelWorkbook actual con el pasado como parametro.
     * Se recomienda realizar la totalizacion a partir de una ExcelWorkBook clonada.
     *
     * @param workbookToTotalize El ExcelWorkbook a a�adir al actual.
     * @param id                 El id de la celda de donde se reflejan el identificador del informe a totalizar.
     *                           Se realizara una concatenacion  de dicho campo en el orden en el que se totaliza en el workbook actual para reflejar el resultado.
     *                           Si se indica null, o un id que no existe, no se realiza accion alguna.
     */
    public void totalize(ExcelWorkbook workbookToTotalize, String id)
    {
        operateValuesWorkbook(workbookToTotalize, TOTALIZE, id);
    }

    /**
     * Realiza la resta de las celdas de la ExcelWorkbook actual con las celdas del Workbook pasado como parametro.
     * Se recomienda realizar la comparacion a partir de una ExcelWorkBook clonada.
     *
     * @param workbookToCompare El ExcelWorkBook con el que se va a realizar la comparacion.
     */
    public void compareWithWorkbook(ExcelWorkbook workbookToCompare)
    {
        operateValuesWorkbook(workbookToCompare, COMPARE, null);
    }

    /**
     * Realiza la operacion indicada sobre el ExcelWorkbook.
     *
     * @param targetWoorkbook El excelWorkbook sobre el que se aplicara la operacion indicada.
     * @param operation       Codigo de operacion a aplicar.
     * @param id              El id de las celdas donde se concatenaran los identificadores de totalizacion.
     */
    private void operateValuesWorkbook(ExcelWorkbook targetWoorkbook, Integer operation, String id)
    {
        Assert.isTrue(!isReadedFromFile, "No es posible totalizar por el momento ExcelWorkbooks leidos de fichero. Deben generarse programaticamente");
        Assert.notNull(targetWoorkbook, "Se debe indicar un ExcelWorkbook no nulo");
        Assert.isTrue(equalsWorkbook(targetWoorkbook), "Los ExcelWorkBooks que se quieren totalizar no son iguales.");
        Assert.notNull(operation, "Se debe indicar un codigo de operacion no nulo");

        cleanHSSFSheets();

        String originalString = null;
        if (logger.isDebugEnabled())
        {
            originalString = this.toString();
        }

        //Recorremos las hojas
        for (int sheetIndex = 0; sheetIndex < this.internalSheets.size(); sheetIndex++)
        {
            ExcelSheet innerExcelSheet = this.internalSheets.get(sheetIndex);
            ExcelSheet targetExcelSheet = targetWoorkbook.internalSheets.get(sheetIndex);

            //Recorremos las filas dentro de cada hoja.
            for (int rowIndex = 0; rowIndex < innerExcelSheet.getRowList().size(); rowIndex++)
            {
                ExcelRow innerRow = innerExcelSheet.getRowList().get(rowIndex);
                ExcelRow targetRow = targetExcelSheet.getRowList().get(rowIndex);

                //Recorremos las celdas de cada fila.
                for (int cellIndex = 0; cellIndex < innerRow.size(); cellIndex++)
                {
                    ExcelCell innerCell = innerRow.getCell(cellIndex);
                    ExcelCell targetCell = targetRow.getCell(cellIndex);
                    if (TOTALIZE.equals(operation))
                    {
                        //Sumamos el contenido de la celda con la celda equivalente del ExcelWorkbook pasado como parametro.
                        totalizeContentValues(innerCell, targetCell, id);
                    }
                    else if (COMPARE.equals(operation))
                    {
                        compareContentValues(innerCell, targetCell);
                    }
                    else
                    {
                        throw new IllegalArgumentException("El codigo '" + operation + "' no se corresponde con ninguna operacion valida en el ExcelWoorkbook");
                    }
                    innerRow.setCell(cellIndex, innerCell);
                }
                innerExcelSheet.setRow(rowIndex, innerRow);
            }

            this.internalSheets.set(sheetIndex, innerExcelSheet);

            if (logger.isDebugEnabled())
            {
                StringBuilder sb = new StringBuilder(200);
                sb.append("Totalize result:");
                sb.append('\n');
                sb.append("Orig->");
                sb.append(originalString);
                sb.append('\n');
                sb.append("Target->");
                sb.append(toString());
                logger.debug(sb.toString());
            }

        }
    }

    /**
     * Limpia la instancia interna del HSSFWorkbook eliminado las HSSFSheet que tenga. Esto es necesario para poder
     * totalizar, y no almacenar las otras hojas que tuviera el ExcelWoorkbook.
     */
    private void cleanHSSFSheets()
    {
        for (int index = 0; index < this.workbook.getNumberOfSheets(); index++)
        {
            this.workbook.removeSheetAt(index);

            if (logger.isDebugEnabled())
            {
                logger.debug("Eliminada HSSFSheet en la posicion :" + index);
            }
        }
    }

    /**
     * Realiza la resta del valor de la celda origen menos el valor de la celda objetivo y guarda el resultado en la celda origen.
     *
     * @param innerCell  La celda origen sobre la que operar.
     * @param targetCell La celdad objetivo contra la que comparar.
     */
    private void compareContentValues(ExcelCell innerCell, ExcelCell targetCell)
    {
        try
        {
            /**
             * Si ambas celdas vienen en la comparacion.
             * Esto se realiza debido a que en Excel, en ocasiones una celda vacia puede venir como cero o nula.
             * Si no se realiza una comprobacion es posible que en algunos casos no controlados se produzca un NullPointerException.
             */
            if (innerCell != null && targetCell != null)
            {
                Object origContent = innerCell.getContentValue();
                Object targetContent = targetCell.getContentValue();

                if (origContent != null && targetContent != null)
                {
                    if (origContent instanceof Number && targetContent instanceof Number)
                    {
                        innerCell.setContentValue(((Number) origContent).doubleValue() - ((Number) targetContent).doubleValue());
                    }
                    else
                    {
                        String msj = "Se trataba de comparar dos celdas cuyo contenido no era el mismo:\n";
                        msj += "OrigContent: " + origContent;
                        msj += "TargetContent: " + targetContent;
                        logger.warn(msj);
                    }
                }
                else if (origContent == null) //si la nula es la primera, si el otro es numero, le cambiamos el signo.
                {
                    if (targetContent instanceof Number)
                    {
                        innerCell.setContentValue(((Number) targetContent).doubleValue() * -1);
                    }
                }
            }
            else if (innerCell == null)
            {
                if (targetCell.getContentValue() != null && targetCell.getContentValue() instanceof Number)
                {
                    //Ponemos la celda objetivo como la origen, cambiando el signo al contenido.
                    innerCell = targetCell.cloneExcelCell();
                }
            }
        }
        catch (CloneNotSupportedException e)
        {
            logger.error(e);
            throw new ExcelEngineException("Se ha producido un error al tratar de comparar las Excel", e);
        }

    }

    /**
     * Compara el actual WorkBook con el indicado como parametro de una forma mas ligera que el equals.
     *
     * @param excelWorkbook El excel woorkbook con el que comparar.
     * @return true si son iguales.
     */
    private boolean equalsWorkbook(ExcelWorkbook excelWorkbook)
    {
        return true;
    }

    /**
     * Suma el contenido de dos celdas. Ambas celdas deben ser no nulas. Si no contienen un valor numerico no se realiza accion alguna.
     * Si una de ellas no tiene contenido no se realiza accion alguna.
     * En caso de que la celda origen sea nula, pero la destino no lo sea, se reemplaza enteramente por la destino.
     * si la destino es nula simplemente no se realiza ninguna accion.
     *
     * @param origCell   La celda destino.
     * @param targetCell La celda origen.
     */
    private void totalizeContentValues(ExcelCell origCell, ExcelCell targetCell, String id)
    {
        try
        {
            if (logger.isDebugEnabled())
            {
                StringBuilder sb = new StringBuilder(150);
                sb.append("Vamos a proceder a la suma de las celdas:\n");
                sb.append("OrigCell: ");
                sb.append(origCell);
                sb.append('\n');
                sb.append("TargetCell: ");
                sb.append(targetCell);
                logger.debug(sb.toString());
            }

            if (targetCell != null)
            {
                if (origCell == null)
                {
                    origCell = targetCell.cloneExcelCell();
                }
                else if (origCell.getContentValue() != null && targetCell.getContentValue() != null)
                {
                    if (id != null && id.length() > 0 && id.equals(origCell.getId()))
                    {
                        Object origValue = origCell.getContentValue();
                        Object targetValue = targetCell.getContentValue();

                        String totalizeString = "";

                        if (origValue != null && origValue.toString().length() > 0)
                        {
                            totalizeString += tratarCeldaConNumeros(origValue.toString(), separators);
                        }

                        if (targetValue != null && targetValue.toString().length() > 0)
                        {
                            if (totalizeString.length() > 0)
                            {
                                totalizeString += " - ";
                            }
                            totalizeString += tratarCeldaConNumeros(targetValue.toString(), separators);
                        }

                        if (totalizeString.length() > 0)
                        {
                            origCell.setContentValue(totalizeString);
                        }
                    }
                    else
                    if (origCell.getContentValue() instanceof Number && targetCell.getContentValue() instanceof Number)
                    {
                        Number origValue = (Number) origCell.getContentValue();
                        Number targetValue = (Number) targetCell.getContentValue();
                        Double sum = origValue.doubleValue() + targetValue.doubleValue();
                        origCell.setContentValue(sum);
                    }
                }
            }
        }
        catch (CloneNotSupportedException e)
        {
            logger.error(e);
            throw new ExcelEngineException("Error durante la suma de la celda origen: " + origCell + " y la destino: " + targetCell, e);
        }

        if (logger.isDebugEnabled())
        {
            logger.debug("Se han sumado las dos celdas resultando: " + origCell);
        }
    }

    /**
     * Trata el contenido de una celda que contenga uno o varios numeros y strings.
     * Se conservan los Strings que sean separadores( ',' y '-' )
     * <b>Siempre se busca utilizando como separador el espacio.</b>
     * @param integerWithString El String de donde obtener el numero que contiene.
     * @return El mismo String, pero tan solo conservando los numeros. Se conservan tambien los Strings que se sean separadores.
     * @param separators El array de separadores que si se encuentran, se deben conservar tal cual. Deben estar ordenados mediante el metodo Arrays.sort(char[])
     */
    public static String tratarCeldaConNumeros(String integerWithString, char[] separators)
    {
        if( logger.isDebugEnabled() )
        {
            logger.debug(integerWithString +" -> tratarCeldaConNumeros" );
        }

        StringBuilder result;

        if( integerWithString != null && integerWithString.length() > 0 )
        {
            //Tratamos el String.
            result = new StringBuilder( integerWithString.length() );
            StringTokenizer st = new StringTokenizer(integerWithString);
            while( st.hasMoreTokens() )
            {
                String token = st.nextToken();

                if( isNumber(token) )
                {
                    result.append(token); //A�adimos solo los numeros.
                }
                else if( token.toCharArray().length == 1 ) //Podria ser un separador
                {
                    int indexOfSeparator = Arrays.binarySearch( separators, token.toCharArray()[0] );
                    if( indexOfSeparator >= 0 )
                    {
                        result.append( ' ' );
                        result.append( separators[indexOfSeparator] );
                        result.append( ' ' );
                    }
                }
            }
        }
        else //No hacemos nada.
        {
            result = new StringBuilder("");
        }
        return result.toString();
    }

    /**
     * Devuelve true si el String es un numero.
     * @param str El String a parsear.
     * @return true si es un numero.
     */
    private static boolean isNumber(String str)
    {
        boolean result = false;

        try
        {
            Integer.parseInt(str);
            result = true;
        }
        catch( NumberFormatException e )
        {
            //Ignoramos la excepcion.
        }

        return result;
    }

    /**
     * Realiza la comparaci�n entre dos objetos ExcelWorkbook. Ambos deben tener el mismo numero de hojas,
     * filas por hoja y celdas por fila de cada hoja.
     *
     * @param excelWorkbook El excelWorkbook objetivo con el que comparar.
     * @return true si ambos tienen el mismo numero de celdas distribuidos de la misma forma.
     */
    @Override
    public boolean equals(Object excelWorkbook)
    {
        boolean result = false;

        if (excelWorkbook != null && excelWorkbook instanceof ExcelWorkbook)
        {
            ExcelWorkbook targetWorkbook = (ExcelWorkbook) excelWorkbook;
            if (targetWorkbook.internalSheets.size() == this.internalSheets.size())
            {
                boolean continueEvaluation = true;
                for (int sheetIndex = 0; sheetIndex < this.internalSheets.size() && continueEvaluation; sheetIndex++)
                {
                    ExcelSheet internalSheet = this.internalSheets.get(sheetIndex);
                    ExcelSheet targetSheet = targetWorkbook.internalSheets.get(sheetIndex);
                    continueEvaluation = internalSheet.equals(targetSheet);
                }
                //Si se ha parado la evaluacion en algun momento es que es false, si no es true.
                result = continueEvaluation;
            }
        }

        if (logger.isDebugEnabled())
        {
            if (result)
            {
                logger.debug("La ExcelWorkbook: " + this.toString() + " es igual que ExcelWorkbook: " + excelWorkbook);
            }
            else
            {
                logger.debug("La ExcelWorkbook: " + this.toString() + " NO es igual que ExcelWorkbook: " + excelWorkbook);
            }
        }

        return result;
    }

    /**
     * Genera una representacion del Workbook indicando numero de filas y celdas por fila.
     *
     * @return Un String con su representacion.
     */
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(200);

        for (int index = 0; index < internalSheets.size(); index++)
        {
            ExcelSheet sheet = internalSheets.get(index);
            sb.append("{ Sheet[");
            sb.append(index);
            sb.append("]");
            sb.append(sheet.toString());
            sb.append(" }");
        }

        return sb.toString();
    }


    /**
     * Clona el ExcelWorkbook actual obteniendo una copia exacta del mismo.
     *
     * @return La copia exacta de actual objeto.
     * @throws CloneNotSupportedException Si no es posible clonar el objeto.
     */
    @Override
    protected Object clone() throws CloneNotSupportedException
    {
        ExcelWorkbook cloneWorkbook = new ExcelWorkbook();

        for (int indexSheet = 0; indexSheet < internalSheets.size(); indexSheet++)
        {
            ExcelSheet excelSheet = (ExcelSheet) internalSheets.get(indexSheet).cloneExcelSheet();
            cloneWorkbook.addSheet(excelSheet);
        }

        cloneWorkbook.workbook = this.workbook;

        cloneWorkbook.setWorkbookName( this.getWorkbookName() );

        return cloneWorkbook;
    }

    /**
     * Realiza una copia exacta de este objeto.
     *
     * @return Una copia exacta del objeto actual.
     * @throws CloneNotSupportedException si no es posible realizar el clon.
     */
    public ExcelWorkbook cloneExcelWorkbook() throws CloneNotSupportedException
    {
        return (ExcelWorkbook) this.clone();
    }

    /**
     * Obtiene el sheet en la posicion indicada.
     *
     * @param excelSheetIndex La posicion del sheet a obtener, comenzando por 0.
     * @return El ExcelSheet de la posicion indicada.
     */
    public ExcelSheet getExcelSheet(Integer excelSheetIndex)
    {
        return this.internalSheets.get(excelSheetIndex);
    }

    /**
     * @see #setCellValue(Integer,String,Integer,Object)
     */
    public void setCellValue(Integer sheetIndex, ExcelPosition excelPosition, Object newValue)
    {
        this.setCellValue(sheetIndex, excelPosition.getColumnLetters(), excelPosition.getRowNum(), newValue);
    }

    /**
     * Establece el valor de la celda indicada.
     *
     * @param sheetIndex    El indice del sheet donde esta la celda, comenzando por 0.
     * @param columnLetters La letra (o letras) de la columna de la celda
     * @param rowNumber     El numero de fila donde esta la excel, comenzando por 1.
     * @param newValue      El nuevo valor a establecer en la celda. Es posible indicar un null.
     */
    public void setCellValue(Integer sheetIndex, String columnLetters, Integer rowNumber, Object newValue)
    {
        Assert.notNull(sheetIndex, "Debe indicar un indice para el ExcelSheet");

        if (isReadedFromFile)
        {
            HSSFSheet sheet = this.workbook.getSheetAt(sheetIndex);
            CellReference cellReference = new CellReference(columnLetters + rowNumber);

            HSSFRow row = sheet.getRow(cellReference.getRow());
            HSSFCell cell = row.getCell(cellReference.getCol());

            setContent(cell, newValue);
            if (logger.isDebugEnabled())
            {
                logger.debug("Establecido el valor: " + newValue + " en la celda de posicion: " + columnLetters + rowNumber + " directamente en el HSSFWorkbook");
            }
        }
        else
        {
            ExcelSheet sheet = getExcelSheet(sheetIndex);
            sheet.setCellValue(columnLetters, rowNumber, newValue);
            this.internalSheets.set(sheetIndex, sheet);
            if (logger.isDebugEnabled())
            {
                logger.debug("Establecido el valor: " + newValue + " en la celda de posicion: " + columnLetters + rowNumber + " en el ExcelWorkbook");
            }
        }
    }

    /**
     * A�ade un nuevo print area al ExcelWoorkbook.
     *
     * @param printArea El nuevo print area a a�adir.
     */
    public void addPrintArea(PrintArea printArea)
    {
        this.printAreas.add(printArea);
    }

    /**
     * Obtiene un String representando este ExcelWorkBook de una forma ligera.
     *
     * @return Un String con su representacion.
     */
    public String asSimpleString()
    {
        StringBuilder sb = new StringBuilder(100);
        sb.append('{');

        for (int index = 0; index < this.internalSheets.size(); index++)
        {
            sb.append(this.internalSheets.get(index).getName());

            if (index < this.internalSheets.size())
            {
                sb.append(',');
            }
        }

        sb.append('}');

        return sb.toString();
    }

    /**
     * Obtiene el valor de la celda indica por la posicion de la celda en formato excel.
     * <br>
     * Ej: A1, AA45, etc.
     *
     * @param sheetIndex    El indice de la hoja donde buscar la hoja.
     * @param columnLetters La letra o letras de la columna.
     * @param rowNumber     El indice de la fila del que obtener la celda.
     * @return La celda correspondiente a la posicion o null si no existe.
     */
    public Object getExcelCellValue(Integer sheetIndex, String columnLetters, Integer rowNumber)
    {
        Assert.notNull(sheetIndex, "Debe indicar un indice para la hoja Excel.");
        Assert.hasLength(columnLetters, "Debe indicar la letra o letras de la columna");
        Assert.notNull(rowNumber, "Debe indicar un row number");

        Object result = null;

        if (logger.isDebugEnabled())
        {
            logger.debug("Vamos a obtener el valor de la celda: " + columnLetters + rowNumber + " en la hoja: " + sheetIndex);
        }

        if (isReadedFromFile)
        {
            HSSFSheet sheet = this.workbook.getSheetAt(sheetIndex);

            CellReference cellReference = new CellReference(columnLetters + rowNumber);
            HSSFRow row = sheet.getRow(cellReference.getRow());
            if (row != null)
            {
                HSSFCell cell = row.getCell(cellReference.getCol());

                if (cell != null)
                {

                    HSSFFormulaEvaluator evaluator = new HSSFFormulaEvaluator(sheet, this.workbook);
                    evaluator.setCurrentRow(row);
                    HSSFFormulaEvaluator.CellValue cellValue = evaluator.evaluate(cell);

                    if (cellValue != null)
                    {

                        int cellType = cellValue.getCellType();

                        if (cellType == HSSFCell.CELL_TYPE_STRING)
                        {
                            result = cellValue.getRichTextStringValue().getString();
                        }
                        else if (cellType == HSSFCell.CELL_TYPE_BOOLEAN)
                        {
                            result = cellValue.getBooleanValue();
                        }
                        else if (cellType == HSSFCell.CELL_TYPE_NUMERIC)
                        {
                            result = cellValue.getNumberValue();
                        }

                        if (logger.isDebugEnabled())
                        {
                            logger.debug("Obtenido valor de HSSFWorkbook: " + result + " de la celda[" + cellType + "] '" + columnLetters + rowNumber + "'");
                        }
                    }
                    else
                    {
                        if (logger.isDebugEnabled())
                        {
                            logger.debug("No existe la celda '" + columnLetters + rowNumber + "' en la hoja numero " + sheetIndex);
                        }
                    }
                }
            }
        }
        else
        {
            ExcelSheet sheet = this.internalSheets.get(sheetIndex);
            result = sheet.getCellValue(columnLetters, rowNumber);

            if (logger.isDebugEnabled())
            {
                logger.debug("Obtenido valor de ExcelWorkbook: " + result + " de la celda '" + columnLetters + rowNumber + "'");
            }
        }


        return result;
    }

    /**
     * Refresca el Workbook actual para actualizar los valores de las formulas en base a los nuevos valores.
     * Para optimizar el rendimiento, tan solo se realiza el refresco si se leyo de disco la excel, no si esta siendo generada
     * programaticamente al ejecutar el metodo.
     */
    public void refreshFormulas()
    {
        if (isReadedFromFile)
        {
            for (int indexSheet = 0; indexSheet < this.workbook.getNumberOfSheets(); indexSheet++)
            {
                HSSFSheet sheet = this.workbook.getSheetAt(indexSheet);
                if (sheet != null)
                {
                    for (int indexRow = 0; indexRow <= sheet.getLastRowNum(); indexRow++)
                    {
                        HSSFRow row = sheet.getRow(indexRow);
                        if (row != null)
                        {
                            for (short indexCell = 0; indexCell < row.getLastCellNum(); indexCell++)
                            {
                                HSSFCell cell = row.getCell(indexCell);
                                refreshHSSFCell(cell);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Realiza el refresco de la celda indicada.
     *
     * @param cell La celda a refrescar.
     */
    private void refreshHSSFCell(HSSFCell cell)
    {
        if (cell != null)
        {
            int cellType = cell.getCellType();
            if (cellType == HSSFCell.CELL_TYPE_FORMULA)
            {
                cell.setCellFormula(cell.getCellFormula());
            }
            else if (cellType == HSSFCell.CELL_TYPE_STRING)
            {
                cell.setCellValue(cell.getRichStringCellValue());
            }
            else if (cellType == HSSFCell.CELL_TYPE_NUMERIC)
            {
                cell.setCellValue(cell.getNumericCellValue());
            }

            if (logger.isDebugEnabled())
            {
                logger.debug("Refrescada celda: " + cell + " de tipo: " + cellType);
            }

        }

    }

    /**
     * Establece si el fichero ya ha sido generado. Se permite su alteracion externa para acceder a ciertas
     * funcionalidades que se permiten una vez haya sido generada la Excel. Debe llamarse previamente al metodo
     * #generateExcel para que la excel sea consistente.
     *
     * @param flag true para indicar que ha sido generado. Por defecto a 'false'.
     */
    public void setInternalWorkbookGenerated(Boolean flag)
    {
        Assert.notNull(flag, "No se permite un flag a null.");
        this.isReadedFromFile = flag;
    }

    /**
     * @see #getExcelCellValue(Integer,String,Integer)
     */
    public Object getExcelCellValue(Integer sheetIndex, ExcelPosition excelPosition)
    {
        Assert.notNull(excelPosition, "Se debe indicar un ExcelPosition no nulo");
        return this.getExcelCellValue(sheetIndex, excelPosition.getColumnLetters(), excelPosition.getRowNum());
    }

    /**
     * Establece el estilo en la posicion indicada.
     *
     * @param sheetIndex  El indice de la hoja comenzando por 0.
     * @param rowIndex    El indice de la fila comenzando por 0.
     * @param columnIndex el indice de la columna comenzando por 0.
     * @param style       El stilo a establecer.
     */
    public void setCellStyle(int sheetIndex, int rowIndex, int columnIndex, HSSFCellStyle style)
    {
        if (isReadedFromFile)
        {
            HSSFSheet sheet = this.workbook.getSheetAt(sheetIndex);
            HSSFRow row = sheet.getRow(rowIndex);
            HSSFCell cell = row.getCell((short) columnIndex);
            cell.setCellStyle(style);
        }
        else
        {
            this.internalSheets.get(sheetIndex).setCellStyle(rowIndex, columnIndex, style);
        }
    }

    /**
     * Obtiene la referencia interna al objeto HSSFWorkbook.
     * <b>Nota:</b> El objeto interno puede no ser consistente en funcion del momento en que se pide, utilice este objeto
     * cuando este seguro que no van a realizar mas procesos en la excel.
     * @return El objeto HSSFWorkbook interno.
     */
    public HSSFWorkbook getInternalWorkbook()
    {
        return this.workbook;
    }


    /**
     * Getter for property 'workbookName'.
     * @return Value for property 'workbookName'.
     * @see #workbookName
     */
    public String getWorkbookName()
    {
        return workbookName;
    }

    /**
     * Setter for property 'workbookName'.
     * @param workbookName Value to set for property 'workbookName'.
     * @see #workbookName
     */
    public void setWorkbookName(String workbookName)
    {
        this.workbookName = workbookName;
    }
}
