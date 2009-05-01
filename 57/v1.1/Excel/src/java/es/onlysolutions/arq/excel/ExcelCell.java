package es.onlysolutions.arq.excel;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;

import java.io.Serializable;

/**
 * Clase que representa una celda del motor de Excel.
 */
public class ExcelCell implements Serializable, Cloneable
{
    /**
     * Es el short correspondiente con el encoding UTF de 16 bits.
     */
    public static final Short ENCODING_UTF_16 = HSSFCell.ENCODING_UTF_16;

    /**
     * Short que representa el encoding COMPRESSED UNICODE.
     */
    public static final Short ENCODING_COMPRESSED_UNICODE = HSSFCell.ENCODING_COMPRESSED_UNICODE;

    private String id;
    private Object contentValue;
    private Integer type;
    private HSSFCellStyle style;
    private String formula;
    private ExcelPosition excelPosition;
    private Short regionWidth;
    private Integer regionHeight;
    private Short encoding;

    /**
     * Constructor sin parametros.
     */
    public ExcelCell()
    {
        super();
    }

    /**
     * Constructor con ID como parametro para simplificar instanciacion.
     *
     * @param id El id con el que construir la celda.
     */
    public ExcelCell(String id)
    {
        this.id = id;
    }

    /**
     * Devuelve el enconding de esta celda como un Short, o null si no se especifico ninguno.
     *
     * @return El encoding de la celda.
     * @see #ENCODING_COMPRESSED_UNICODE
     * @see #ENCODING_UTF_16
     */
    public Short getEncoding()
    {
        return encoding;
    }

    /**
     * Establece el encoding de la celda. Si se indica un valor null no se especifica encoding alguno.
     *
     * @param encoding El encoding a especificar.
     * @see #ENCODING_COMPRESSED_UNICODE
     * @see #ENCODING_UTF_16
     */
    public void setEncoding(Short encoding)
    {
        this.encoding = encoding;
    }

    /**
     * Getter for property 'regionHeight'.
     *
     * @return Value for property 'regionHeight'.
     * @see #regionHeight
     */
    public Integer getRegionHeight()
    {
        return regionHeight;
    }

    /**
     * Setter for property 'regionHeight'.
     *
     * @param regionHeight Value to set for property 'regionHeight'.
     * @see #regionHeight
     */
    public void setRegionHeight(Integer regionHeight)
    {
        this.regionHeight = regionHeight;
    }

    /**
     * Getter for property 'regionWidth'.
     *
     * @return Value for property 'regionWidth'.
     * @see #regionWidth
     */
    public Short getRegionWidth()
    {
        return regionWidth;
    }

    /**
     * Setter for property 'regionWidth'.
     *
     * @param regionWidth Value to set for property 'regionWidth'.
     * @see #regionWidth
     */
    public void setRegionWidth(Short regionWidth)
    {
        this.regionWidth = regionWidth;
    }

    /**
     * Getter for property 'id'.
     *
     * @return Value for property 'id'.
     * @see #id
     */
    public String getId()
    {
        return id;
    }

    /**
     * ID unico para la identificacion de la celda. No pueden existir dos ID repetidos.
     *
     * @param id Value to set for property 'id'.
     * @see #id
     */
    public void setId(String id)
    {
        this.id = id;
    }

    /**
     * Getter for property 'contentValue'.
     *
     * @return Value for property 'contentValue'.
     * @see #contentValue
     */
    public Object getContentValue()
    {
        return contentValue;
    }

    /**
     * Establece el valor de la celda. Es posible almacenar distintos tipos de objetos (Calendar, String, Double, Boolean y Date).
     *
     * @param contentValue El valor a establecer en la celda.
     */
    public void setContentValue(Object contentValue)
    {
        this.contentValue = contentValue;
    }

    /**
     * Getter for property 'type'.
     *
     * @return Value for property 'type'.
     * @see #type
     */
    public Integer getType()
    {
        return type;
    }

    /**
     * Establece el tipo de la celda. Debe ser un tipo valido soportado por la clase HSSFCell.
     *
     * @param type Value to set for property 'type'.
     * @see #type
     * @see org.apache.poi.hssf.usermodel.HSSFCell#setCellType(int)
     */
    public void setType(Integer type)
    {
        this.type = type;
    }

    /**
     * Getter for property 'style'.
     *
     * @return Value for property 'style'.
     * @see #style
     * @see org.apache.poi.hssf.usermodel.HSSFCellStyle
     */
    public HSSFCellStyle getStyle()
    {
        return style;
    }

    /**
     * Setter for property 'style'.
     *
     * @param style Value to set for property 'style'.
     * @see #style
     * @see org.apache.poi.hssf.usermodel.HSSFCellStyle
     */
    public void setStyle(HSSFCellStyle style)
    {
        this.style = style;
    }

    /**
     * Getter for property 'formula'.
     *
     * @return Value for property 'formula'.
     * @see #formula
     */
    public String getFormula()
    {
        return formula;
    }

    /**
     * Setter for property 'formula'.
     *
     * @param formula Value to set for property 'formula'.
     * @see #formula
     */
    public void setFormula(String formula)
    {
        this.formula = formula;
    }

    /**
     * Establece la posicion en la tabla excel. Es un valor interno que asigna la matriz segun se a�adan celdas.
     *
     * @param position La posicion de la tabla excel (indicando fila y columna).
     */
    void setExcelPosition(ExcelPosition position)
    {
        this.excelPosition = position;
    }

    /**
     * Obtiene la posicion de la celda en la hoja excel.
     *
     * @return La posicion de la celda en la hoja excel.
     */
    ExcelPosition getExcelPosition()
    {
        return this.excelPosition;
    }


    public String toString()
    {
        return "ExcelCell{" + "id='" + id + '\'' + ", contentValue=" + contentValue + ", formula='" + formula + '\'' + ", excelPosition=" + excelPosition + ", regionWidth=" + regionWidth + ", regionHeight=" + regionHeight + '}';
    }

    /**
     * Compara si dos celdas son iguales. El tipo del objeto que contienen debe ser igual, o ambas deben contener formulas.
     * Si una de las dos es una celda en blanco y la otra no devuelve siempre false. Aunque tengan el mismo contenido.
     *
     * @param obj La celda objetivo.
     * @return true si ambas celdas son iguales.
     * @see es.onlysolutions.arq.excel.ExcelRow#BLANK_CELL
     */
    @Override
    public boolean equals(Object obj)
    {
        boolean result = false;

        if (obj != null && obj instanceof ExcelCell)
        {
            ExcelCell targetCell = (ExcelCell) obj;
            result = this.id.equals(targetCell.getId());

//            if (ExcelRow.BLANK_CELL.getId().equals(targetCell.getId()) && ExcelRow.BLANK_CELL.getId().equals(this.id))
//            {
//                result = true;
//            }
//            else if (ExcelRow.BLANK_CELL.getId().equals(targetCell.getId()) || ExcelRow.BLANK_CELL.getId().equals(this.id))
//            {
//                result = false;
//            }
//            else
//            {
//
//                Object targetContentValue = targetCell.getContentValue();
//                Object innerContentValue = this.getContentValue();
//                if (innerContentValue != null)
//                {
//                    result = innerContentValue.equals(targetContentValue);
//                }
//                else
//                {
//                    if (targetContentValue == null) //ambas pueden ser formulas
//                    {
//                        result = this.getFormula() != null && targetCell.getFormula() != null;
//                    }
//                }
//            }
        }

        return result;
    }

    /**
     * Realiza una copia exacta de la celda.
     *
     * @return Una copia exacta de la celda.
     * @throws CloneNotSupportedException
     */
    @Override
    protected Object clone() throws CloneNotSupportedException
    {
        String cloneId = null;
        if (this.id != null)
        {
            cloneId = String.valueOf(id);
        }

        Object cloneContentValue = this.contentValue;

        Integer cloneType = null;
        if (this.type != null)
        {
            cloneType = Integer.valueOf(this.type);
        }

        HSSFCellStyle cloneStyle = this.style;

        String cloneFormula = null;
        if (this.formula != null)
        {
            cloneFormula = String.valueOf(this.formula);
        }

        ExcelPosition cloneExcelPosition = null;
        if (this.excelPosition != null)
        {
            cloneExcelPosition = this.excelPosition.cloneExcelPosition();
        }

        Short cloneRegionWidth = null;
        if (this.regionWidth != null)
        {
            cloneRegionWidth = Short.valueOf(this.regionWidth);
        }

        Integer cloneRegionHeight = null;
        if (this.regionHeight != null)
        {
            cloneRegionHeight = Integer.valueOf(this.regionHeight);
        }

        Short cloneEncoding = null;
        if (this.encoding != null)
        {
            cloneEncoding = Short.valueOf(this.encoding);
        }

        ExcelCell cell = new ExcelCell();
        cell.setId(cloneId);
        cell.setContentValue(cloneContentValue);
        cell.setEncoding(cloneEncoding);
        cell.setExcelPosition(cloneExcelPosition);
        cell.setRegionWidth(cloneRegionWidth);
        cell.setRegionHeight(cloneRegionHeight);
        cell.setFormula(cloneFormula);
        cell.setStyle(cloneStyle);
        cell.setType(cloneType);

        return cell;
    }

    /**
     * Devuelve una copia exacta de esta clase llamand
     *
     * @return Una copia exacta de esta clase.
     * @throws CloneNotSupportedException Si no es posible clonar este objeto.
     * @see #clone()
     */
    public ExcelCell cloneExcelCell() throws CloneNotSupportedException
    {
        return (ExcelCell) this.clone();
    }
}
