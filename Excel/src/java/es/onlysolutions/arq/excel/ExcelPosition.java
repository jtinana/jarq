package es.onlysolutions.arq.excel;

import org.springframework.util.Assert;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Representa una posicion en una tabla Excel, letras para la columna y numeros para la fila.
 */
public class ExcelPosition implements Serializable, Cloneable
{
    /**
     * Array con las letras del abecedario en mayusculas y sin la �.
     */
    private static final Character[] ALPHABET = new Character[]{'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};

    /**
     * Ordenamos el Array para asegurarnos que la busqueda binaria en correcta.
     */
    static
    {
        Arrays.sort(ALPHABET);
    }

    /**
     * El numero de letras del alfabeto.
     */
    private static final int ALPHABET_SIZE = 26;

    private Integer rowNum;
    private String columnLetters;

    /**
     * Constructor para una posicion.
     *
     * @param rowNum        El numero de fila.
     * @param columnLetters La letra de la columna.
     */
    public ExcelPosition(Integer rowNum, String columnLetters)
    {
        this.rowNum = rowNum;
        this.columnLetters = columnLetters;
    }

    /**
     * Getter for property 'rowNum'.
     *
     * @return Value for property 'rowNum'.
     * @see #rowNum
     */
    public Integer getRowNum()
    {
        return rowNum;
    }

    /**
     * Setter for property 'rowNum'.
     *
     * @param rowNum Value to set for property 'rowNum'.
     * @see #rowNum
     */
    public void setRowNum(Integer rowNum)
    {
        this.rowNum = rowNum;
    }

    /**
     * Getter for property 'columnLetters'.
     *
     * @return Value for property 'columnLetters'.
     * @see #columnLetters
     */
    public String getColumnLetters()
    {
        return columnLetters;
    }

    /**
     * Setter for property 'columnLetters'.
     *
     * @param columnLetters Value to set for property 'columnLetters'.
     * @see #columnLetters
     */
    public void setColumnLetters(String columnLetters)
    {
        this.columnLetters = columnLetters;
    }

    /**
     * Convierte la posicion indicada en su equivalente a las columnas en Excel (A, B, C ..., AA, AB, ...).<br>
     * <b>Tan solo soporta columnas de hasta dos letras</b>
     *
     * @param columnPosition La posicion a convertir.
     * @return El valor valido para la posicion.
     */
    public static String converIntValueToLetters(int columnPosition)
    {
        String lettersResult;

        if (columnPosition < ALPHABET_SIZE)
        {
            Character letter = ALPHABET[columnPosition];
            lettersResult = letter.toString();
        }
        else
        {
            int result = (columnPosition / ALPHABET_SIZE) - 1;
            Character firstChar = ALPHABET[result];

            int modResult = columnPosition % ALPHABET_SIZE;
            Character secondChar = ALPHABET[modResult];

            lettersResult = firstChar.toString() + secondChar.toString();
        }

        return lettersResult;
    }

    /**
     * Convierte una posicion en excel dada a la posicion de la columna que corresponde.<br>
     * Dada la posicion 'A', se devuelve el valor 1, y si se indica el valor 'AJ', se devuelve el valor 36.<br>
     * <b>Tan solo soporta columnas de hasta dos letras</b>
     *
     * @param columnLetters La letra o letras de la columna.
     * @return El valor entero que corresponde con dicha columna.
     */
    public static Integer convertLetterColumnToInteger(String columnLetters)
    {
        Assert.hasLength(columnLetters, "Se debe indicar un valor de columna no vacio (F, AA, etc)");
        char[] charColumns = columnLetters.toCharArray();
        Assert.isTrue(charColumns.length <= 2, "No se soportan columnas de mas de dos letras.");

        Integer total;

        if (charColumns.length == 1)
        {
            total = Arrays.binarySearch(ALPHABET, charColumns[0]) + 1;
        }
        else //tienen que ser dos porque si no habra saltado el Assert.
        {
            total = ((Arrays.binarySearch(ALPHABET, charColumns[0]) + 1) * ALPHABET_SIZE) + (Arrays.binarySearch(ALPHABET, charColumns[1]) + 1);
        }

        return total;
    }

    /**
     * Obtiene una representacion en Excel de la posicion en las celdas.
     *
     * @return La repsentacion de la posicion en la Excel de esta posicion.
     */
    @Override
    public String toString()
    {
        return columnLetters + rowNum;
    }

    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        ExcelPosition that = (ExcelPosition) o;

        if (rowNum != that.rowNum)
        {
            return false;
        }
        if (!columnLetters.equals(that.columnLetters))
        {
            return false;
        }

        return true;
    }

    public int hashCode()
    {
        int result;
        result = rowNum;
        result = 31 * result + columnLetters.hashCode();
        return result;
    }


    /**
     * Obtiene una copia del ExcelPosition actual.
     *
     * @return Un copia exacta del ExcelPosition actual.
     * @throws CloneNotSupportedException Si no es posible clonar el objeto.
     */
    @Override
    protected Object clone() throws CloneNotSupportedException
    {
        return new ExcelPosition(Integer.valueOf(this.rowNum), String.valueOf(this.columnLetters));
    }

    /**
     * Ejecuta el metodo clone sobre el objeto y realiza una casting al objeto ExcelPosition.
     *
     * @return Una nueva instancia de la clase actual.
     * @throws CloneNotSupportedException Si no es posible clonar el objeto.
     * @see #clone()
     */
    public ExcelPosition cloneExcelPosition() throws CloneNotSupportedException
    {
        return (ExcelPosition) this.clone();
    }
}
