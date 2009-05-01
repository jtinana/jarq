package es.onlysolutions.arq.excel;

import java.io.Serializable;

/**
 * Print Area a�adido a un ExcelWorkbook.
 */
public class PrintArea implements Serializable
{
    private Integer sheetIndex;
    private Integer startColumn;
    private Integer endColumn;
    private Integer startRow;
    private Integer endRow;

    /**
     * Constructor sin parametros.
     */
    public PrintArea()
    {
        super();
    }

    /**
     * Constructor con todos los parametros.
     */
    public PrintArea(Integer sheetIndex, Integer startColumn, Integer endColumn, Integer startRow, Integer endRow)
    {
        this.sheetIndex = sheetIndex;
        this.startColumn = startColumn;
        this.endColumn = endColumn;
        this.startRow = startRow;
        this.endRow = endRow;
    }

    /**
     * Getter for property 'sheetIndex'.
     *
     * @return Value for property 'sheetIndex'.
     * @see #sheetIndex
     */
    public Integer getSheetIndex()
    {
        return sheetIndex;
    }

    /**
     * Setter for property 'sheetIndex'.
     *
     * @param sheetIndex Value to set for property 'sheetIndex'.
     * @see #sheetIndex
     */
    public void setSheetIndex(Integer sheetIndex)
    {
        this.sheetIndex = sheetIndex;
    }

    /**
     * Getter for property 'startColumn'.
     *
     * @return Value for property 'startColumn'.
     * @see #startColumn
     */
    public Integer getStartColumn()
    {
        return startColumn;
    }

    /**
     * Setter for property 'startColumn'.
     *
     * @param startColumn Value to set for property 'startColumn'.
     * @see #startColumn
     */
    public void setStartColumn(Integer startColumn)
    {
        this.startColumn = startColumn;
    }

    /**
     * Getter for property 'endColumn'.
     *
     * @return Value for property 'endColumn'.
     * @see #endColumn
     */
    public Integer getEndColumn()
    {
        return endColumn;
    }

    /**
     * Setter for property 'endColumn'.
     *
     * @param endColumn Value to set for property 'endColumn'.
     * @see #endColumn
     */
    public void setEndColumn(Integer endColumn)
    {
        this.endColumn = endColumn;
    }

    /**
     * Getter for property 'startRow'.
     *
     * @return Value for property 'startRow'.
     * @see #startRow
     */
    public Integer getStartRow()
    {
        return startRow;
    }

    /**
     * Setter for property 'startRow'.
     *
     * @param startRow Value to set for property 'startRow'.
     * @see #startRow
     */
    public void setStartRow(Integer startRow)
    {
        this.startRow = startRow;
    }

    /**
     * Getter for property 'endRow'.
     *
     * @return Value for property 'endRow'.
     * @see #endRow
     */
    public Integer getEndRow()
    {
        return endRow;
    }

    /**
     * Setter for property 'endRow'.
     *
     * @param endRow Value to set for property 'endRow'.
     * @see #endRow
     */
    public void setEndRow(Integer endRow)
    {
        this.endRow = endRow;
    }
}
