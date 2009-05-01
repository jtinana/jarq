package es.onlysolutions.arq.excel;

/**
 * Wrapper sobre la clase HSSFPrintSetup de POI.
 */
public class ExcelPrintSetup
{

    /**
     * Indica si es apaisado o no. Por defecto a false.
     */
    private Boolean landScape = false;

    /**
     * Indica el modo de impresion.
     */
    private Boolean lefToRight = false;

    /**
     * Establece el valor para el fit Height.
     */
    private Short fitHeight;

    /**
     * Establece el valor para el Fit Width.
     */
    private Short fitWidth;

    /**
     * Constructor sin parametros.
     */
    public ExcelPrintSetup()
    {
        super();
    }

    /**
     * Consctructor con landscape y leftToRight.
     *
     * @param landScape  El landScape.
     * @param lefToRight El lefto Right.
     */
    public ExcelPrintSetup(Boolean landScape, Boolean lefToRight)
    {
        this.landScape = landScape;
        this.lefToRight = lefToRight;
    }


    /**
     * Getter for property 'fitHeight'.
     *
     * @return Value for property 'fitHeight'.
     * @see #fitHeight
     */
    public Short getFitHeight()
    {
        return fitHeight;
    }

    /**
     * Setter for property 'fitHeight'.
     *
     * @param fitHeight Value to set for property 'fitHeight'.
     * @see #fitHeight
     */
    public void setFitHeight(Short fitHeight)
    {
        this.fitHeight = fitHeight;
    }

    /**
     * Getter for property 'fitWidth'.
     *
     * @return Value for property 'fitWidth'.
     * @see #fitWidth
     */
    public Short getFitWidth()
    {
        return fitWidth;
    }

    /**
     * Setter for property 'fitWidth'.
     *
     * @param fitWidth Value to set for property 'fitWidth'.
     * @see #fitWidth
     */
    public void setFitWidth(Short fitWidth)
    {
        this.fitWidth = fitWidth;
    }

    /**
     * Getter for property 'lefToRight'.
     *
     * @return Value for property 'lefToRight'.
     * @see #lefToRight
     */
    public Boolean getLefToRight()
    {
        return lefToRight;
    }

    /**
     * Setter for property 'lefToRight'.
     *
     * @param lefToRight Value to set for property 'lefToRight'.
     * @see #lefToRight
     */
    public void setLefToRight(Boolean lefToRight)
    {
        this.lefToRight = lefToRight;
    }

    /**
     * Getter for property 'landScape'.
     *
     * @return Value for property 'landScape'.
     * @see #landScape
     */
    public Boolean getLandScape()
    {
        return landScape;
    }

    /**
     * Setter for property 'landScape'.
     *
     * @param landScape Value to set for property 'landScape'.
     * @see #landScape
     */
    public void setLandScape(Boolean landScape)
    {
        this.landScape = landScape;
    }

    /**
     * Realiza una copia exacta del ExcelPrintSetup actual.
     * @return El ExcelPrintSetup clonado.
     * @throws CloneNotSupportedException Si ocurre algun error durante la clonacion.
     */
    public ExcelPrintSetup clonePrintSetup() throws CloneNotSupportedException
    {
        return (ExcelPrintSetup) this.clone();
    }

    /**
     * Realiza una copia exacta del ExcelPrintSetup actual.
     * @return El ExcelPrintSetup clonado.
     * @throws CloneNotSupportedException Si ocurre algun error durante la clonacion.
     */
    @Override
    protected Object clone() throws CloneNotSupportedException
    {
        ExcelPrintSetup cloneResult = new ExcelPrintSetup();
        cloneResult.setFitHeight( this.fitHeight );
        cloneResult.setFitWidth( this.fitWidth );
        cloneResult.setLandScape( this.landScape );
        cloneResult.setLefToRight( this.lefToRight );
        return cloneResult;
    }
}
