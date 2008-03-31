package es.onlysolutions.arq.excel;

import es.onlysolutions.arq.core.log.LoggerGenerator;
import es.onlysolutions.arq.core.service.utils.EntityUtils;
import org.apache.commons.logging.Log;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.util.Region;
import org.springframework.util.Assert;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Matriz que contiene todos los objetos celda para generar la representacion.
 */
public class ExcelSheet implements Serializable, Cloneable
{
    /**
     * Separador que se utilizara para distinguir los ID. Estara colocado al principio y al final de cada ID en las formulas.
     */
    public static final String ID_SEPARATOR = "#";

    /**
     * El logger de la clase.
     */
    public static final Log logger = LoggerGenerator.getLogger(ExcelSheet.class);

    /**
     * Ancho de columnas por defecto.
     */
    private Short defaultColumnWidth;

    /**
     * Indica si se ha activado el autobreak en el sheet.
     */
    private Boolean autoBreak = false;

    /**
     * La matriz interna con las celdas.
     */
    private List<ExcelRow> matrix;

    /**
     * Las regiones que existen en el sheet actual.
     */
    private List<Region> regions = new ArrayList<Region>(3);

    /**
     * El PrintSetup interno con las opciones de impresion.
     */
    private ExcelPrintSetup printSetup;

    /**
     * Posicion de columns break.
     */
    private Short columnBreak;

    /**
     * La posicion de la fila donde se rompe la pagina.
     */
    private Integer rowBreak;
    /**
     * Valor para establecer la propiedad de Fit To Page.
     */
    private Boolean fitToPage;
    /**
     * Establece si este sheet esta centrado verticalmente.
     */
    private Boolean verticallyCenter;

    /**
     * Establece si el sheet esta centrado horizontalmente.
     */
    private Boolean horizontallyCenter;

    /**
     * Anchos establecidos para las columnas.
     */
    private Map<Short, Short> columnWidths = new HashMap<Short, Short>(7);

    /**
     * Mapa que guarda las relaciones entre los id de cada celda y su posicion en la excel.
     */
    private Map<String, ExcelPosition> associationIdPosition = new HashMap<String, ExcelPosition>(100);

    /**
     * El nombre de la matriz.
     */
    private String name;
    /**
     * Establece si este sheet es el seleccionado en el libro.
     */
    private Boolean selected;

    /**
     * A�ade una region al ExcelSheet. Son regiones que se tendran en cuenta a la hora de leer un fichero, y no ofrecen
     * con metodos publicos.
     *
     * @param reg La region a a�adir.
     */
    void addRegion(Region reg)
    {
        this.regions.add(reg);
    }

    /**
     * Obtiene el numero de regiones que contiene el ExcelSheet.
     *
     * @return El numero de regiones.
     */
    public int getNumberOfRegions()
    {
        return this.regions.size();
    }

    /**
     * Obtiene la region en el indice indicado.
     *
     * @param index El indice del que obtener la region.
     * @return El objeto Region.
     */
    public Region getRegion(int index)
    {
        return this.regions.get(index);
    }

    /**
     * Constructor sin parametros.
     *
     * @param matrixName El nombre que se dara a la hoja que representa la matriz.
     */
    public ExcelSheet(String matrixName)
    {
        this(matrixName, 10);
    }

    /**
     * Constructor indicando el tama�o inicial que tendra la matriz.
     * Inicialmente se inicializara a ySize la altura de la matriz, y cada una de las filas se ir� inicializando
     * seg�n se creen con xSize.
     *
     * @param matrixName El nombre de la hoja que representa la matriz.
     * @param ySize      El tama�o inicial del numero de filas.
     */
    public ExcelSheet(String matrixName, int ySize)
    {
        this.matrix = new ArrayList<ExcelRow>(ySize);
        this.name = matrixName;
    }

    /**
     * Obtiene el nombre la hoja excel.
     *
     * @return El nombre de la hoja excel.
     */
    public String getSheetName()
    {
        return this.name;
    }

    /**
     * A�ade una nueva fila a la matriz. Si el parametro es null, se a�adira una fila de celdas vacias.
     *
     * @param newRow La nueva fila a a�adir.
     */
    public void addRow(ExcelRow newRow)
    {
        Assert.notNull(newRow, "No se puede a�adir un elemento null a la matriz");
        this.matrix.add(newRow);
    }

    /**
     * Obtiene el nombre de la matrix. Se utilizara para dar nombre a la hoja de la excel.
     *
     * @return El nombre de la matriz.
     */
    public String getName()
    {
        return name;
    }

    /**
     * Obtiene el numero total de rows que tiene la matrix.
     *
     * @return El recuento de filas de la matriz.
     */
    public int getRowCount()
    {
        return this.matrix.size();
    }

    /**
     * Obtiene el numero de celdas de la fila indicada.
     *
     * @param rowIndex El numero de fila del que obtener el conteo de celdas. Debe ser un numero valido.
     * @return El numero de celdas de la fila.
     * @throws IllegalArgumentException Si la posicion no es valida.
     */
    public int getCellCount(int rowIndex)
    {
        rangeRowCheck(rowIndex);

        return this.matrix.get(rowIndex).size();
    }

    private void rangeRowCheck(int rowIndex)
    {
        if (rowIndex >= this.matrix.size())
        {
            throw new IllegalArgumentException("El numero de fila '" + rowIndex + "' no existe en la matriz. El numero de filas total es: " + this.matrix.size());
        }
    }

    /**
     * Obtiene la celda de la fila indicada y en la posicion indicada dentro de esa fila.
     *
     * @param rowIndex  El numero de fila.
     * @param cellIndex El numero de celda a obtener.
     * @return La celda de la posicion indicada.
     * @throws IllegalArgumentException Si alguna de las posiciones no son validas.
     */
    public ExcelCell getCell(int rowIndex, int cellIndex)
    {
        rangeCellCheck(rowIndex, cellIndex);
        return this.matrix.get(rowIndex).getCell(cellIndex);
    }

    /**
     * Verifica que el rango de la celda indicada existe y es valida. Tambien verifica implicitamente el rango de la fila.
     *
     * @param rowIndex  El rango de la fila.
     * @param cellIndex El rango de la celda.
     */
    private void rangeCellCheck(int rowIndex, int cellIndex)
    {
        rangeRowCheck(rowIndex);
        if (cellIndex >= this.matrix.get(rowIndex).size())
        {
            throw new IllegalArgumentException("El numero de celda '" + cellIndex + "' de la fila '" + rowIndex + "' no existe");
        }
    }

    /**
     * Este metodo se debe ejecutar una vez relleno la matrix. Asocia a cada celda la posicion que ocupa en la hoja excel
     * indicando numero de fila y letra de columna.
     */
    public void establishCellPositions()
    {
        this.clearAssociationIds();
        for (int rowIndex = 0; rowIndex < matrix.size(); rowIndex++)
        {
            ExcelRow cells = matrix.get(rowIndex);
            for (int cellIndex = 0; cellIndex < cells.size(); cellIndex++)
            {
                ExcelCell excelCell = cells.getCell(cellIndex);

                /**
                 * Si la celda viene a null es que no hay que introducir nada, o si la celda es BLANK.
                 * @see es.onlysolutions.arq.excel.ExcelRow#BLANK_CELL
                 */
                if (excelCell != null && !ExcelRow.UNIQUE_ID.equals(excelCell.getId()))
                {
                    ExcelPosition excelPosition = new ExcelPosition(rowIndex + 1, ExcelPosition.converIntValueToLetters(cellIndex));
                    excelCell.setExcelPosition(excelPosition);
                    cells.setCell(cellIndex, excelCell);

                    checkIdNotExist(excelCell);
                    this.associationIdPosition.put(excelCell.getId(), excelCell.getExcelPosition());
                }
            }
            matrix.set(rowIndex, cells);
        }
    }

    /**
     * Comprueba que el id no exista ya previamente en el mapa para evitar errores.
     *
     * @param cell La celda de la que comprobar si existe su id.
     * @throws IllegalArgumentException Si el id ya existe.
     */
    private void checkIdNotExist(ExcelCell cell)
    {
        if (this.associationIdPosition.containsKey(cell.getId()))
        {
            throw new IllegalArgumentException("Se ha duplicado el ID: " + cell.getId() + " en la celda '" + cell + "', revise los IDs de las celdas");
        }
    }

    /**
     * Obtiene la posicion en la Excel asociada al id indicado.
     *
     * @param id El id del que obtener la posicion en la excel.
     * @return La posicion en la Excel que el id debe ocupar, o null si no existe.
     */
    public ExcelPosition getExcelPositionById(String id)
    {
        ExcelPosition excelPosition = null;

        excelPosition = this.associationIdPosition.get(id);

        if (excelPosition == null) //Intentamos buscarlo en la hoja actual.
        {
            for (int indexRow = 0; indexRow < this.matrix.size(); indexRow++)
            {
                ExcelRow row = this.matrix.get(indexRow);
                for (int indexColumn = 0; indexColumn < row.size(); indexColumn++)
                {
                    ExcelCell cell = row.getCell(indexColumn);
                    if (cell.getId().equals(id))
                    {
                        Integer rowNum = indexRow;
                        String columnLetters = ExcelPosition.converIntValueToLetters(indexColumn);
                        excelPosition = new ExcelPosition(rowNum + 1, columnLetters);
                    }
                }
            }
        }

        return excelPosition;
    }

    /**
     * Obtiene la lista de ExcelRow de esta matriz.
     *
     * @return La lista de ExcelRow de la matriz.
     */
    public List<ExcelRow> getRowList()
    {
        return this.matrix;
    }

    /**
     * Obtiene el ancho por defecto establecido para las columnas.
     *
     * @return El ancho por defecto, null si no se ha establecido ninguno.
     */
    public Short getDefaultColumnWidth()
    {
        return defaultColumnWidth;
    }

    /**
     * Establece el ancho por defecto para las columnas. Si se indica un null, al generarse la Excel se estableceran internamente.
     *
     * @param defaultColumnWidth El valor para el ancho de las columnas.
     */
    public void setDefaultColumnWidth(Short defaultColumnWidth)
    {
        this.defaultColumnWidth = defaultColumnWidth;
    }

    /**
     * Establece el ancho indicado para una columna.
     *
     * @param columnIndex El indice de la columna a establecer, comenzando por 0.
     * @param columnWidth El ancho a establecer para esa columna.
     */
    public void setColumnWidth(Short columnIndex, Short columnWidth)
    {
        Assert.notNull(columnIndex, "Se debe indicar un INDICE no nulo para la columna.");
        Assert.notNull(columnWidth, "Se debe indicar un ANCHO no nulo para la columna.");
        this.columnWidths.put(columnIndex, columnWidth);
    }

    /**
     * Establece el ancho indicado para una columna. Realiza una conversion de los valores Integer a Short.
     *
     * @param columIndex El indice de la columna. Se convertira a Short.
     * @param columWidth El ancho de la columna. Se realizara la conversion a Short.
     * @see #setColumnWidth(Short,Short)
     */
    public void setColumnWidth(Integer columIndex, Integer columWidth)
    {
        setColumnWidth(columIndex.shortValue(), columWidth.shortValue());
    }

    /**
     * Obtiene el ancho de la columna referenciada por el indice. Devuelve null si el indice de la columna no existe.
     *
     * @param columnIndex El indice de la columna de la que obtener el ancho. No se permite un valor null.
     * @return El ancho de la columna. null si dicha columna no existe.
     */
    public Short getColumnWidth(Short columnIndex)
    {
        Assert.notNull(columnIndex, "Se debe indicar un INDICE de columna no nulo");
        return this.columnWidths.get(columnIndex);
    }

    /**
     * Obtiene el mapa de anchos de las columnas.
     *
     * @return El mapa con los anchos de cada columna.
     */
    Map<Short, Short> getColumnWidths()
    {
        return this.columnWidths;
    }

    /**
     * Obtiene el valor de la propiedad Fit To Page.
     *
     * @return El valor de esta propiedad.
     */
    public Boolean getFitToPage()
    {
        return fitToPage;
    }

    /**
     * Establece el valor para la propiedad Fit To Page de esta hoja. Por defecto se establece a true.
     *
     * @param fitToPage El valor a establecer.
     * @see org.apache.poi.hssf.usermodel.HSSFSheet#setFitToPage(boolean)
     */
    public void setFitToPage(Boolean fitToPage)
    {
        this.fitToPage = fitToPage;
    }

    /**
     * Compara dos ExcelSheet teniendo en cuenta si tienen el mismo numero de filas y si estas devuelven true al ejecutar
     * el equals sobre cada una de ellas entre la original y la ExcelSheet objetivo.
     *
     * @param obj La ExcelSheet objetivo.
     * @return true si son iguales.
     */
    @Override
    public boolean equals(Object obj)
    {
        boolean result = false;

        if (obj != null && obj instanceof ExcelSheet)
        {
            ExcelSheet sheet = (ExcelSheet) obj;
            if (sheet.getRowCount() == this.getRowCount())
            {
                boolean continueEvaluation = true;
                for (int rowIndex = 0; rowIndex < sheet.getRowCount() && continueEvaluation; rowIndex++)
                {
                    ExcelRow targetRow = sheet.getRowList().get(rowIndex);
                    ExcelRow innerRow = this.getRowList().get(rowIndex);
                    continueEvaluation = targetRow.equals(innerRow);
                }
                result = continueEvaluation;
            }
        }

        return result;
    }

    /**
     * Establece un ExcelRow en la posicion indicada. Dicha posicion debe ser valida.
     *
     * @param rowIndex el indice donde a�adir el row.
     * @param row      El row a a�adir.
     * @see java.util.List#set(int,Object)
     */
    public void setRow(int rowIndex, ExcelRow row)
    {
        this.matrix.set(rowIndex, row);
    }

    /**
     * Genera un String con la representacion del ExcelSheet.
     *
     * @return Un String con la representacion del String.
     */
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(200);

        for (int index = 0; index < this.matrix.size(); index++)
        {
            ExcelRow row = this.matrix.get(index);
            sb.append(" - Row[");
            sb.append(index);
            sb.append("]");
            sb.append(row.toString());
        }

        return sb.toString();
    }

    /**
     * Limpia el mapa de asociaciones de ID con sus posiciones.
     */
    void clearAssociationIds()
    {
        this.associationIdPosition.clear();
    }


    /**
     * Obtiene una copia exacta del objeto actual.
     *
     * @return Una copia exacta del objeto actual.
     * @throws CloneNotSupportedException Si no es posible realizar el clon.
     */
    @Override
    protected Object clone() throws CloneNotSupportedException
    {
        String cloneName = null;
        if (this.name != null)
        {
            cloneName = String.valueOf(this.name);
        }
        ExcelSheet sheetResult = new ExcelSheet(cloneName);

        Map<Short, Short> cloneColumnWidths = EntityUtils.cloneHashMap(this.columnWidths);
        sheetResult.columnWidths = cloneColumnWidths;

        Map<String, ExcelPosition> cloneAssociationIdPosition = EntityUtils.cloneHashMap(this.associationIdPosition);
        sheetResult.associationIdPosition = cloneAssociationIdPosition;

        List<ExcelRow> cloneMatrix = EntityUtils.cloneArrayList(this.matrix);
        sheetResult.matrix = cloneMatrix;

        if (this.defaultColumnWidth != null)
        {
            sheetResult.setDefaultColumnWidth( Short.valueOf(this.defaultColumnWidth) );
        }

        if (fitToPage != null)
        {
            sheetResult.setFitToPage( Boolean.valueOf(this.fitToPage) );
        }

        if( this.printSetup != null )
        {
            sheetResult.setPrintSetup( this.printSetup.clonePrintSetup() );
        }

        if( this.verticallyCenter != null )
        {
            sheetResult.setVerticallyCenter( Boolean.valueOf(this.verticallyCenter) );
        }

        if( this.horizontallyCenter != null )
        {
            sheetResult.setHorizontallyCenter(Boolean.valueOf(this.horizontallyCenter) );
        }

        if( this.autoBreak != null )
        {
            sheetResult.setAutoBreak( Boolean.valueOf(this.autoBreak) );
        }
        

        return sheetResult;
    }

    /**
     * Realiza un clon del objeto actual.
     *
     * @return Una copia exacta del objeto ExcelSheet.
     * @throws CloneNotSupportedException Si no es posible clonar el objeto.
     * @see #clone()
     */
    public ExcelSheet cloneExcelSheet() throws CloneNotSupportedException
    {
        return (ExcelSheet) this.clone();
    }

    /**
     * Establece el nuevo valor indicado en la celda referenciada mediante un nombre de excel. El ExcelSheet se encarga de
     * convertir dichos valores internamente para encontrar la celda.
     *
     * @param columnLetters La letra, o letras de la columna en excel.
     * @param rowNumber     El numero de fila. Comenzando por 1.
     * @param newValue      El nuevo valor a establecer en la celda. Es posible indicar un valor null.
     */
    public void setCellValue(String columnLetters, Integer rowNumber, Object newValue)
    {
        ExcelRow row = this.getRowList().get(rowNumber - 1);

        if (logger.isDebugEnabled())
        {
            logger.debug("Celdas en la fila: " + row);
            logger.debug("Se va a establecer el valor : " + newValue + " en la celda: " + columnLetters + rowNumber);
        }

        Integer cellPosition = ExcelPosition.convertLetterColumnToInteger(columnLetters) - 1;
        ExcelCell cell = row.getCell(cellPosition);
        cell.setContentValue(newValue);
        row.setCell(cellPosition, cell);
        this.setRow(rowNumber - 1, row);
    }

    /**
     * Establece si el sheet actual estara centrado verticalmente.
     *
     * @param flag True si se desea centrar, false o null en caso contrario.
     */
    public void setVerticallyCenter(Boolean flag)
    {
        this.verticallyCenter = flag;
    }

    /**
     * Establece si el sheet actual estara centrado horizontalmente.
     *
     * @param flag true si se desea centrar, false o null en caso contrario.
     */
    public void setHorizontallyCenter(Boolean flag)
    {
        this.horizontallyCenter = flag;
    }

    /**
     * Obtiene si el sheet actual sera centrado verticalmente.
     *
     * @return true si afirmativo, false o null en caso contrario.
     */
    public Boolean getVerticallyCenter()
    {
        return verticallyCenter;
    }

    /**
     * Obtiene si el sheet actual sera centrado horizontalmente.
     *
     * @return true si afirmativo, false o null en caso contrario.
     */
    public Boolean getHorizontallyCenter()
    {
        return horizontallyCenter;
    }


    /**
     * Establece el column break en la posicion indicada.
     *
     * @param columnBreak La posicion de la columna donde establecer la ruptura de pagina.
     */
    public void setColumnBreak(Short columnBreak)
    {
        this.columnBreak = columnBreak;
    }

    /**
     * Obtiene la actual posicion de la columna donde se establecio el column break.
     *
     * @return La posicion del column break.
     */
    public Short getColumnBreak()
    {
        return columnBreak;
    }

    /**
     * Obtiene la fila donde se establecio el row break.
     *
     * @return La posicion de la fila del Row Break.
     */
    public Integer getRowBreak()
    {
        return rowBreak;
    }

    /**
     * Establece el Row Break.
     *
     * @param rowBreak El row break (posicion de la fila).
     */
    public void setRowBreak(Integer rowBreak)
    {
        this.rowBreak = rowBreak;
    }

    /**
     * Obtiene el valor actual para el autobreak.
     *
     * @return true si esta activado.
     */
    public Boolean getAutoBreak()
    {
        return autoBreak;
    }

    /**
     * Establece el valor para autobreak.
     *
     * @param autoBreak true si se desea activar, false en caso contrario.
     */
    public void setAutoBreak(Boolean autoBreak)
    {
        Assert.notNull(autoBreak, "Debe indicar un valor no nulo para el autobreak");
        this.autoBreak = autoBreak;
    }


    /**
     * Getter for property 'printSetup'.
     *
     * @return Value for property 'printSetup'.
     * @see #printSetup
     */
    public ExcelPrintSetup getPrintSetup()
    {
        return printSetup;
    }

    /**
     * Setter for property 'printSetup'.
     *
     * @param printSetup Value to set for property 'printSetup'.
     * @see #printSetup
     */
    public void setPrintSetup(ExcelPrintSetup printSetup)
    {
        this.printSetup = printSetup;
    }

    /**
     * Establece el sheet actual como sheet seleccionado.
     *
     * @param flag True si se desea seleccionar, false o null en caso contrario.
     */
    public void setSelected(Boolean flag)
    {
        this.selected = flag;
    }

    /**
     * Obtiene el valor de la celda indicada como letra de columna y numero de fila.
     *
     * @param columnLetters La letra o letras de la columna.
     * @param rowNumber     El indice de la columna. Comienza por 1.
     * @return El valor de la celda o null si es formula o no tiene.
     */
    public Object getCellValue(String columnLetters, Integer rowNumber)
    {
        ExcelRow row = this.getRowList().get(rowNumber - 1);

        if (logger.isDebugEnabled())
        {
            logger.debug("Obtenido el row de indice: " + (rowNumber - 1) + " -> " + row.toString());
        }

        Integer cellPosition = ExcelPosition.convertLetterColumnToInteger(columnLetters) - 1;

        if (logger.isDebugEnabled())
        {
            logger.debug("Convertimos la columna '" + columnLetters + "' a la posicion interna: " + cellPosition);
        }

        ExcelCell cell = row.getCell(cellPosition);

        if (logger.isDebugEnabled())
        {
            logger.debug("Se obtiene la celda: " + cell);
        }

        return cell.getContentValue();
    }

    /**
     * Establece una celda en la posicion indicada de la ExcelSheet.
     *
     * @param rowIndex      El indice de la fila comenzado desde 0.
     * @param columnIndex   El indice de la columna comenzando desde 0.
     * @param cellToReplace La celda a reemplazar.
     */
    public void setCell(int rowIndex, int columnIndex, ExcelCell cellToReplace)
    {
        ExcelRow row = this.matrix.get(rowIndex);
        row.setCell(columnIndex, cellToReplace);
        this.matrix.set(rowIndex, row);
    }

    
    public void setCellStyle(int rowIndex, int columnIndex, HSSFCellStyle style)
    {
        ExcelRow row = this.matrix.get(rowIndex);
        ExcelCell cell = row.getCell(columnIndex);
        cell.setStyle(style);
        row.setCell(columnIndex, cell);
        this.matrix.set(rowIndex, row);
    }
}
