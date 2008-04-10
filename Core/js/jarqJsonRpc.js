// Se debe modificar el nombre de aplicacion para cada nuevo proyecto.
//Metodo para realizar una llamada al servidor JSON.
// bridgeName -> Nombre del Bridge de JSON registrado en la session.
// methodName -> Nombre del metodo a ejecutar en el bridge.
// args -> Array de argumentos a pasarle al metodo, si es null o vacio no se pasa nada al metodo.
// return -> El resultado de la ejecucion del metodo remoto.
// callBackFunction -> Funcion JavaScript para utilizar como CallBack. Esta funcion debe recibir dos argumentos, el primero sera el
//resultado, y el segundo la excepcion, que sea nula en caso de ir correcto. Si se indica un valor, la funcion jasonCall no
// devuelve nada.
function jsonEntityArrayCall(bridgeName, methodName, args, callBackFunction)
{
    try
    {
        var jsonrpc = new JSONRpcClient("/GI_Proyectos/JSON-RPC");
        var expression = 'jsonrpc.' + bridgeName + '.' + methodName + '(';

        //si se ha indicado Callback function la ponemos como primer argumento.
        if (callBackFunction != null && callBackFunction.length > 0)
        {
            expression += callBackFunction + ', ';
        }

        //Recorremos los argumentos para pasarselos al metodo.
        if (args != null)
        {
            for (var i = 0; i < args.length; i++)
            {
                expression += args[i];
                if (args.length > (i + 1))
                {
                    expression += ',';
                }
            }
        }

        expression += ')';

        var result = eval(expression);

        if (callBackFunction == null || callBackFunction.length == 0)
        {
            var evaluatedResult = eval("(" + toJSON(result.list) + ")");
            return evaluatedResult;
        }
        else
        {
            return null;
        }
    }
    catch (e)
    {
        alert(e);
        return null;
    }
}



//Oculta un elemento por ID.
function f_hideElementById(elementId)
{
    if (document.getElementById(elementId))
    {
        document.getElementById(elementId).style.visibility = 'hidden';
    }
}

//Hace visible un elemento indicado por id.
function f_showElement(elementId)
{
    var elementToShow = document.getElementById(elementId);
    elementToShow.style.visibility = 'visible';
}

//Rellena el combo con el ID 'comboId' utilizando el array 'jsonPlainObjectArray'. El parametro idField sera el campo
// del bean a utilizar como value del combo, y el textField el campo del bean a poner como label.
function f_fillUpCombo(comboId, jsonPlainObjectArray, idField, textField)
{
    var selectElement = document.getElementById(comboId);
    var index = selectElement.options.length;
    for (var i = 0; i < jsonPlainObjectArray.length; i++)
    {
        selectElement.options[i + index] = new Option(jsonPlainObjectArray[i][textField], jsonPlainObjectArray[i][idField]);
    }
}
//Elimina el elemento de la posicion seleccionada del combo
function f_removeElementCombo(comboId, index)
{
    var selectElement = document.getElementById(comboId);
    selectElement.options.remove(index);
}
//Vacia el combo indicado por ID.
function f_emptyCombo(comboId)
{
    var selectElement = document.getElementById(comboId);
    for (var i = 0; i < selectElement.options.length; i++)
    {
        selectElement.options[i] = null;
    }
}

//Realiza un trim del String pasado como parametro.
function trim(cadena)
{
    for (var i = 0; i < cadena.length;)
    {
        if (cadena.charAt(i) == " ")
            cadena = cadena.substring(i + 1, cadena.length);
        else
            break;
    }

    for (var i = cadena.length - 1; i >= 0; i = cadena.length - 1)
    {
        if (cadena.charAt(i) == " ")
            cadena = cadena.substring(0, i);
        else
            break;
    }

    return cadena;
}

//Obtiene el valor seleccionado actualmente del combo indicado por ID.
function f_obtenerValorComboPorId(comboId)
{
    if (comboId == null)
    {
        throw "No se puede indicar un ID a null";
    }
    var comboElement = document.getElementById(comboId);
    if (comboElement != null)
    {
        //		alert('comboElement.selectedIndex= ' + comboElement.selectedIndex);
        if (comboElement.selectedIndex > -1)
        {
            var comboValue = comboElement.options[comboElement.selectedIndex].value;
//			alert('comboValue= ' + comboValue);
            if (comboValue != '')
            {
                return comboElement.options[comboElement.selectedIndex].value;
            }
            else
            {
                return null;
            }
        }
        else
        {
            return null;
        }
    }
    else
    {
        //		alert("Ha indicado un id que no existe: " + comboId);
        return null;
    }
}

//Obtiene el numero de elementos que contiene el combo actualmente.
function f_comboLenght(idCombo)
{
    var combo = document.getElementById(idCombo);
    return combo.options.length;
}

//Llamada a JSON para la obtencion de una entidad unicamente.
function jsonEntityCall(bridgeName, methodName, args)
{
    try
    {
        var jsonrpc = new JSONRpcClient("/GI_Proyectos/JSON-RPC");
        var expression = 'jsonrpc.' + bridgeName + '.' + methodName + '(';

        //Recorremos los argumentos para pasarselos al metodo.
        if (args != null)
        {
            for (var i = 0; i < args.length; i++)
            {
                expression += args[i];
                if (args.length > (i + 1))
                {
                    expression += ',';
                }
            }
        }

        expression += ')';

        var result = eval(expression);

        var evaluatedResult = eval('(' + toJSON(result) + ')');
        return evaluatedResult;
    }
    catch (e)
    {
        alert(e);
        return null;
    }
}

//Obtiene el valor del atributo indicado de la entidad que se le pasa como parametro.
function f_obtenerAttEntidad(entidad, atributo)
{
    if (entidad == null)
    {
        throw "Debe pasarse una entidad como parametro";
    }

    if (atributo == null)
    {
        throw "Debe pasarse un atributo como parametro";
    }

    return entidad[atributo];
}

//Establece en valor vacio, o -1 del combo indicado por id.Si el combo no posee un option con valor '' o -1 no hace nada.
function f_resetCombo(comboId)
{
    if (document.getElementById(comboId))
    {
        var selectElement = document.getElementById(comboId);
        for (var indexOption = 0; indexOption < selectElement.options.length; indexOption++)
        {
            selectElement.options[selectElement.selectedIndex].selected = false;
            if (selectElement.options[indexOption].value == '' ||
                selectElement.options[indexOption].value == '-1' ||
                selectElement.options[indexOption].value == -1)
            {
                //Establecemos ese valor como seleccionado y quitamos el que esta.
                selectElement.options[indexOption].selected = true;
            }
        }
    }
}