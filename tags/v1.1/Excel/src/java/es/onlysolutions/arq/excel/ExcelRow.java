package es.onlysolutions.arq.excel;

import es.onlysolutions.arq.core.log.LoggerGenerator;
import org.apache.commons.logging.Log;
import org.springframework.util.Assert;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Representa una fila de celdas. Permite la inserccion por posicion de celdas, manejando los huevos de forma apropiada.
 */
public class ExcelRow implements Serializable, Cloneable
{

    /**
     * El logger de la clase.
     */
    private static final Log logger = LoggerGenerator.getLogger(ExcelRow.class);

    /**
     * ID unico para la celda.
     */
    public static final String UNIQUE_ID = "UNIQUE_ID[" + System.currentTimeMillis() + ']';

    /**
     * Celda en blanco con indice unico que se utiliza de forma interna para rellenar huecos.
     * Esta celda no sera tratada para ningun proceso, y no se debe utilizar de forma externa al componente.
     * @return Una nueva celda en blanco. Todas las celdas en blanco tienen como ID el UNIQUE_ID.
     */
    public static ExcelCell getBLANK_CELL()
    {
        ExcelCell cellBlank = new ExcelCell(UNIQUE_ID);
        cellBlank.setContentValue(" ");
        return cellBlank;
    }

    /**
     * La lista interna de celdas.
     */
    private List<ExcelCell> row;

    private Float heightInPoints;

    /**
     * Obtiene la altura en puntos indicada para esta fila.
     *
     * @return La altura de esta fila, null si no se ha indicado ninguna.
     */
    public Float getHeightInPoints()
    {
        return heightInPoints;
    }

    /**
     * Establece la altura para esta fila. Si se indica null no se indicara altura alguna.
     *
     * @param heightInPoints La altura en puntos.
     */
    public void setHeightInPoints(Float heightInPoints)
    {
        this.heightInPoints = heightInPoints;
    }

    /**
     * Constructor sin parametros.
     */
    public ExcelRow()
    {
        this.row = new ArrayList<ExcelCell>(10);
    }

    /**
     * Constructor con tama�o inicial de la fila.
     *
     * @param initialSize El tama�o inicial de la fila
     */
    public ExcelRow(int initialSize)
    {
        this.row = new ArrayList<ExcelCell>(initialSize);
    }

    /**
     * A�ade una celda a la fila en la posicion indicada.
     *
     * @param position  La posicion en la que a�adir la celda.
     *                  Los huecos que se dejen en la lista se rellenaran con celdas en blanco.
     * @param excelCell La celda a a�adir. No se permiten valores nulos.
     */
    public void addCell(int position, ExcelCell excelCell)
    {
        Assert.notNull(excelCell, "No se permiten a�adir valores nulos.");
        Assert.isTrue(position >= 0, "La posicion debe ser un valor mayor o igual que 0");
        if (position > this.row.size())
        {
            int diff = position - this.row.size();
            for (int i = 0; i < diff; i++)
            {
                this.row.add(getBLANK_CELL());
            }
        }
        this.row.add(position, excelCell);
    }

    /**
     * A�ade una nueva celda sin indicar su posicion.
     *
     * @param excelCell La nueva celda a a�adir.
     */
    public void addCell(ExcelCell excelCell)
    {
        Assert.notNull(excelCell, "No se pueden a�adir celdas a null");
        this.row.add(excelCell);
    }

    /**
     * Obtiene el tama�o de esta fila.
     *
     * @return El tama�o de la fila.
     */
    public int size()
    {
        return this.row.size();
    }

    /**
     * Obtiene la celda en la posicion indicada.
     *
     * @param index La posicion de la celda a obtener, comenzado por 0.
     * @return La celda de la posicion indicada.
     */
    public ExcelCell getCell(int index)
    {
        ExcelCell result;
        if (index >= this.row.size())
        {
            logger.warn("La posicion '" + index + "' no es una posicion de la fila, el tama�o de la fila es: " + this.row.size());
            result = new ExcelCell();
        }
        else
        {
            result = this.row.get(index);
        }
        return result;
    }

    /**
     * Establece una celda en la posicion indicada sustiyendo a la previa.
     *
     * @param index        El indica de la celda a sustituir.
     * @param newExcelCell La nueva celda a establecer.
     * @see java.util.ArrayList#set(int,Object)
     */
    public void setCell(int index, ExcelCell newExcelCell)
    {
        if (index < this.row.size())
        {
            this.row.set(index, newExcelCell);
        }
    }

    /**
     * Obtiene un iterator de las celdas de la fila.
     *
     * @return El Iterator<ExcelCell> instanciado.
     */
    public Iterator<ExcelCell> iterator()
    {
        return this.row.iterator();
    }

    /**
     * Compara dos objetos ExcelRow y devuelve true si tienen el mismo numero de celdas y todas son iguales.
     *
     * @param obj El ExcelRow objetivo.
     * @return true si son iguales.
     */
    @Override
    public boolean equals(Object obj)
    {
        boolean result = false;

        if (obj != null && obj instanceof ExcelRow)
        {
            ExcelRow row = (ExcelRow) obj;
            if (row.size() == this.size())
            {
                boolean continueEvaluation = true;
                for (int cellIndex = 0; cellIndex < row.size() && continueEvaluation; cellIndex++)
                {
                    ExcelCell targetCell = row.getCell(cellIndex);
                    ExcelCell innerCell = this.getCell(cellIndex);
                    continueEvaluation = targetCell.equals(innerCell);
                }

                result = continueEvaluation;
            }
        }

        return result;
    }

    /**
     * Obtiene una representacion del ExcelRow actual.
     *
     * @return Un String con la representacion del ExcelRow actual.
     */
    @Override
    public String toString()
    {
        return "(" + this.row.size() + ')';
    }

    /**
     * Realiza una copia del objeto actual.
     *
     * @return Un copia exacta del objeto actual.
     * @throws CloneNotSupportedException Si no es posible clonar el objeto.
     */
    @Override
    public Object clone() throws CloneNotSupportedException
    {
        ExcelRow cloneRow = new ExcelRow(this.size());

        for (int indexCell = 0; indexCell < this.row.size(); indexCell++)
        {
            ExcelCell cellToClone = this.row.get(indexCell);

            if (cellToClone != null)
            {
                cloneRow.addCell(cellToClone.cloneExcelCell());
            }
        }

        Float cloneHeightInPoints = null;
        if (this.getHeightInPoints() != null)
        {
            cloneHeightInPoints = Float.valueOf(this.getHeightInPoints());
        }
        cloneRow.setHeightInPoints(cloneHeightInPoints);

        return cloneRow;
    }

    /**
     * Realiza una copia exacta del objeto.
     *
     * @return Una copia exacta del objeto.
     * @throws CloneNotSupportedException Si no es posible clonar el objeto.
     * @see #clone()
     */
    public ExcelRow cloneExcelRow() throws CloneNotSupportedException
    {
        return (ExcelRow) this.clone();
    }
}
