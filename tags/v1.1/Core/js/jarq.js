//Funciones para listados del DisplayTag. No modificar salvo que se desee modificar la forma en que se seleccionan los listados.
/** Variables globales con las rutas relativas a las imagenes del filtro **/
var ultimaFilaMarcada = '-1';
var ultimoEstiloMarcado = '';
var bw = lib_bwcheck();

function marcarFilaDisplayTag(id)
{
    if (id == ultimaFilaMarcada)
    {
        desmarcarFila(id);
        ultimaFilaMarcada = '-1'
    }
    else
    {
        desmarcarFila(ultimaFilaMarcada);
        marcarFila(id);
        ultimaFilaMarcada = id;
    }
}

function marcarFila(id)
{
    ultimoEstiloMarcado = document.getElementById(id).className;
    document.getElementById(id).className = 'filaSeleccionadaCssDisplayTag';
}

function desmarcarFila(id)
{
    if (id > -1)
    {
        document.getElementById(id).className = ultimoEstiloMarcado;
    }
}

//Funcion para expandir el filtro.
function expandirFiltro(ioNode, button)
{
    var nodeState;
    if (document.getElementById && document.getElementById(ioNode) != null)
    {
        nodeState = document.getElementById(ioNode).className;
    }
    if (nodeState == 'collapsed')
    {
        expand(ioNode, button);
    }
    else
    {
        collapse(ioNode, button);
    }
}

function expand(ioNode, button)
{
    if (document.getElementById && document.getElementById(ioNode) != null)
    {
        var buttonElement = document.getElementById(button);
        if (buttonElement != null)
        {
            buttonElement.title = 'collapse';
            buttonElement.className = 'btnFiltroExpanded';
        }
        document.getElementById(ioNode).className = 'expanded';
    }
}

function collapse(ioNode, button)
{
    if (document.getElementById && document.getElementById(ioNode) != null)
    {
        if (document.getElementById(button) != null)
        {
            document.getElementById(button).title = 'expand';
            document.getElementById(button).className = 'btnFiltro';
        }
        document.getElementById(ioNode).className = 'collapsed';
    }
}
function lib_bwcheck()
{ //Browsercheck (needed)
    this.ver = navigator.appVersion
    this.agent = navigator.userAgent
    this.dom = document.getElementById ? 1 : 0
    this.opera5 = (navigator.userAgent.indexOf("Opera") > -1 && document.getElementById) ? 1 : 0
    this.ie5 = (this.ver.indexOf("MSIE 5") > -1 && this.dom && !this.opera5) ? 1 : 0;
    this.ie6 = (this.ver.indexOf("MSIE 6") > -1 && this.dom && !this.opera5) ? 1 : 0;
    this.ie4 = (document.all && !this.dom && !this.opera5) ? 1 : 0;
    this.ie = this.ie4 || this.ie5 || this.ie6
    this.mac = this.agent.indexOf("Mac") > -1
    this.ns6 = (this.dom && parseInt(this.ver) >= 5) ? 1 : 0;
    this.ns4 = (document.layers && !this.dom) ? 1 : 0;
    this.bw = (this.ie6 || this.ie5 || this.ie4 || this.ns4 || this.ns6 || this.opera5)
    return this
}





// Funciones JavaScript utilizdas por la arquitectura de forma interna.
// Cualquier cambio o modificacion debe ser concordante con los distintos Tags y clases de presentacion de la aquitectura.


//Variable global donde se va almacenando el mensaje de validacion.
var arq_validationTextMsj = "";

//Array donde se almacenan los id´s de los errores de validacion añadidos. Se borran en cada chequeo.
var arq_error_fields_array = new Array();

//Array con los ID de los campos iluminados con el 'cssErrorFieldStyle'.
var arq_highLight_fields_array = new Array();

//Constantes para modificar el comportamiento del JavaScript, en funcion de las necesidades de la aplicacion.

// Constante para indicar si se desean iluminar los campos con el estilo indicado en la constante 'cssErrorFieldStyle'
var arq_highLightFields = true;

// Constante para indicar si se desea mostrar el mensaje encima del campo con el estilo 'cssErrorMsgStyle' o bien
// acumularlo en la variable 'arq_validationTextMsj' para mostrarlo mas tarde en un alert.
var arq_showFieldMessage = true;

// Variable que se actualiza a true en cuanto se encuentre algun error de validacion. Se utilizara para mostrar los mensajes
// en la funcion principal.
var arq_hasErrors = false;

// Estilo css para el mensaje erroneo.
var cssErrorMsgStyle = 'inputError';

// Estilos css para el campo erroneo.
var cssErrorFieldStyle = 'campoError';

// *********************************************************************************************************** //
// *********************** Funciones internas del JS para realizar las tareas de validacion ******************
// *********************************************************************************************************** //

var MONTH_NAMES = new Array('Enero', 'Febrero', 'Marzo', 'Abril', 'Mayo', 'Junio', 'Julio', 'Agosto', 'Septiembre', 'Octubre', 'Noviembre', 'Diciembre', 'Ene', 'Feb', 'Mar', 'Abr', 'May', 'Jun', 'Jul', 'Ago', 'Sep', 'Oct', 'Nov', 'Dic');
var DAY_NAMES = new Array('Domingo', 'Lunes', 'Martes', 'Miércoles', 'Jueves', 'Viernes', 'Sábado', 'Dom', 'Lun', 'Mar', 'Mié', 'Jue', 'Vie', 'Sáb');
function LZ(x)
{
    return(x < 0 || x > 9 ? "" : "0") + x;
}
function _isInteger(val)
{
    var digits = "1234567890";
    for (var i = 0; i < val.length; i++)
    {
        if (digits.indexOf(val.charAt(i)) == -1)
        {
            return false;
        }
    }
    return true;
}
function _getInt(str, i, minlength, maxlength)
{
    for (var x = maxlength; x >= minlength; x--)
    {
        var token = str.substring(i, i + x);
        if (token.length < minlength)
        {
            return null;
        }
        if (_isInteger(token))
        {
            return token;
        }
    }
    return null;
}


/*** Obtiene una fecha de una cadena con el formato indicado ***/
function getDateFromFormat(val, format)
{
    val = val + "";
    format = format + "";
    var i_val = 0;
    var i_format = 0;
    var c = "";
    var token = "";
    var token2 = "";
    var x, y;
    var now = new Date();
    var year = now.getYear();
    var month = now.getMonth() + 1;
    var date = 1;
    var hh = now.getHours();
    var mm = now.getMinutes();
    var ss = now.getSeconds();
    var ampm = "";
    while (i_format < format.length)
    {
        c = format.charAt(i_format);
        token = "";
        while ((format.charAt(i_format) == c) && (i_format < format.length))
        {
            token += format.charAt(i_format++);
        }
        if (token == "yyyy" || token == "yy" || token == "y")
        {
            if (token == "yyyy")
            {
                x = 4;
                y = 4;
            }
            if (token == "yy")
            {
                x = 2;
                y = 2;
            }
            if (token == "y")
            {
                x = 2;
                y = 4;
            }
            year = _getInt(val, i_val, x, y);
            if (year == null)
            {
                return 0;
            }
            i_val += year.length;
            if (year.length == 2)
            {
                if (year > 70)
                {
                    year = 1900 + (year - 0);
                }
                else
                {
                    year = 2000 + (year - 0);
                }
            }
        }
        else if (token == "MMM" || token == "NNN")
        {
            month = 0;
            for (var i = 0; i < MONTH_NAMES.length; i++)
            {
                var month_name = MONTH_NAMES[i];
                if (val.substring(i_val, i_val + month_name.length).toLowerCase() == month_name.toLowerCase())
                {
                    if (token == "MMM" || (token == "NNN" && i > 11))
                    {
                        month = i + 1;
                        if (month > 12)
                        {
                            month -= 12;
                        }
                        i_val += month_name.length;
                        break;
                    }
                }
            }
            if ((month < 1) || (month > 12))
            {
                return 0;
            }
        }
        else if (token == "EE" || token == "E")
        {
            for (var i = 0; i < DAY_NAMES.length; i++)
            {
                var day_name = DAY_NAMES[i];
                if (val.substring(i_val, i_val + day_name.length).toLowerCase() == day_name.toLowerCase())
                {
                    i_val += day_name.length;
                    break;
                }
            }
        }
        else if (token == "MM" || token == "M")
        {
            month = _getInt(val, i_val, token.length, 2);
            if (month == null || (month < 1) || (month > 12))
            {
                return 0;
            }
            i_val += month.length;
        }
        else if (token == "dd" || token == "d")
        {
            date = _getInt(val, i_val, token.length, 2);
            if (date == null || (date < 1) || (date > 31))
            {
                return 0;
            }
            i_val += date.length;
        }
        else if (token == "hh" || token == "h")
        {
            hh = _getInt(val, i_val, token.length, 2);
            if (hh == null || (hh < 1) || (hh > 12))
            {
                return 0;
            }
            i_val += hh.length;
        }
        else if (token == "HH" || token == "H")
        {
            hh = _getInt(val, i_val, token.length, 2);
            if (hh == null || (hh < 0) || (hh > 23))
            {
                return 0;
            }
            i_val += hh.length;
        }
        else if (token == "KK" || token == "K")
        {
            hh = _getInt(val, i_val, token.length, 2);
            if (hh == null || (hh < 0) || (hh > 11))
            {
                return 0;
            }
            i_val += hh.length;
        }
        else if (token == "kk" || token == "k")
        {
            hh = _getInt(val, i_val, token.length, 2);
            if (hh == null || (hh < 1) || (hh > 24))
            {
                return 0;
            }
            i_val += hh.length;
            hh--;
        }
        else if (token == "mm" || token == "m")
        {
            mm = _getInt(val, i_val, token.length, 2);
            if (mm == null || (mm < 0) || (mm > 59))
            {
                return 0;
            }
            i_val += mm.length;
        }
        else if (token == "ss" || token == "s")
        {
            ss = _getInt(val, i_val, token.length, 2);
            if (ss == null || (ss < 0) || (ss > 59))
            {
                return 0;
            }
            i_val += ss.length;
        }
        else if (token == "a")
        {
            if (val.substring(i_val, i_val + 2).toLowerCase() == "am")
            {
                ampm = "AM";
            }
            else if (val.substring(i_val, i_val + 2).toLowerCase() == "pm")
            {
                ampm = "PM";
            }
            else
            {
                return 0;
            }
            i_val += 2;
        }
        else
        {
            if (val.substring(i_val, i_val + token.length) != token)
            {
                return 0;
            }
            else
            {
                i_val += token.length;
            }
        }
    }
    if (i_val != val.length)
    {
        return 0;
    }
    if (month == 2)
    {
        if (((year % 4 == 0) && (year % 100 != 0)) || (year % 400 == 0))
        {
            if (date > 29)
            {
                return 0;
            }
        }
        else
        {
            if (date > 28)
            {
                return 0;
            }
        }
    }
    if ((month == 4) || (month == 6) || (month == 9) || (month == 11))
    {
        if (date > 30)
        {
            return 0;
        }
    }
    if (hh < 12 && ampm == "PM")
    {
        hh = hh - 0 + 12;
    }
    else if (hh > 11 && ampm == "AM")
    {
        hh -= 12;
    }
    var newdate = new Date(year, month - 1, date, hh, mm, ss);
    return newdate.getTime();
}


//Inserta un nuevo error de validacion con el mensaje indicado.
function _arq_insertValidationMsg(fieldId, msg)
{
    if (arq_showFieldMessage)
    {
        var loginObject = document.getElementById(fieldId);
        arq_highLight_fields_array[arq_highLight_fields_array.length] = new Array(fieldId, loginObject.className);
        loginObject.className = cssErrorMsgStyle;

        var item = document.createElement('span');
        item.className = cssErrorFieldStyle;
        item.innerHTML = msg;

        var errorFieldId = 'arq_errorElement_' + arq_error_fields_array.length;
        item.id = errorFieldId;
        arq_error_fields_array[arq_error_fields_array.length] = errorFieldId;
        var form = loginObject.parentNode;
        form.insertBefore(item, loginObject);
    }
    else
    {
        arq_validationTextMsj += msg;
    }
    //Ha habido un error.
    arq_hasErrors = true;
}


// **************************************************************************************************************** //
// **************************************************************************************************************** //
// **************************************************************************************************************** //

//                                  FUNCIONES DE VALIDACION PARA EL TAG JSP.

// **************************************************************************************************************** //
// **************************************************************************************************************** //
// **************************************************************************************************************** //


//Valida si un campo es obligatorio o no.
//El fieldID es el id del campo (debe indicarse un id en la JSP).
//El fieldAlias es el nombre que se dara al campo en los mensajes.
function arq_validateRequiredField(fieldId, fieldAlias)
{
    var fieldObject = document.getElementById(fieldId);
    var msg = "El campo " + fieldAlias + " es obligatorio";

    if (fieldObject.value == null || fieldObject.value.length == 0)
    {
        _arq_insertValidationMsg(fieldId, msg);
    }
}

//Valida la longitud del campo indicado. Si el campo no existe o es nulo se da por valido.
// fieldId -> El id del campo a validar.
// fieldAlias -> El alias a utilizar para los mensajes.
// maxLenght -> La longitud maxima a validar.
function arq_validateLenghField(fieldId, fieldAlias, maxLenght)
{
    var fieldObject = document.getElementById(fieldId);
    var msg = "El campo " + fieldAlias + " excede de su longitud maxima [" + maxLenght + "]";
    if (fieldObject != null)
    {
        if (fieldObject.value)
        {
            if (fieldObject.value.length > maxLenght)
            {
                _arq_insertValidationMsg(fieldId, msg);
            }
        }
    }
}

// Valida si el contenido del campo es un valor numerico. Si el campo no existe o es nulo se da por valido.
// fieldId -> El id del campo a validar.
// fieldAlias -> El alias del campo a utilizar en los mensajes.
function arq_isNumberField(fieldId, fieldAlias)
{
    var fieldObject = document.getElementById(fieldId);
    var msg = "El campo " + fieldAlias + " debe ser numerico";

    if (fieldObject != null && fieldObject.value && fieldObject.value.length > 0)
    {
        if (isNaN(fieldObject.value))
        {
            _arq_insertValidationMsg(fieldId, msg);
        }
    }
}

// Comprueba si el campo es una fecha valida.
// fieldId -> El id del campo a validar.
// fieldAlias -> El alias del campo para mostrar en los mensajes de error.
function arq_isDate(fieldId, fieldAlias, formatPattern)
{
    var fieldObject = document.getElementById(fieldId);
    var msg = "El campo " + fieldAlias + "no contiene una fecha valida. (" + formatPattern + ")";

    if (fieldObject && fieldObject.value && fieldObject.value.length > 0)
    {
        var dateInMills = getDateFromFormat(fieldObject.value, formatPattern);
        if (dateInMills == 0)
        {
            _arq_insertValidationMsg(fieldId, msg);
        }
    }
}

// Comprueba si en campo cumple con un formato de email valido. 
// fieldId -> El id del campo a validar.
// fieldAlias -> El alias del campo para mostrar en los mensajes de error.
function arq_isEmail(fieldId, fieldAlias)
{
    var fieldObject = document.getElementById(fieldId);
    var msg = "El campo " + fieldAlias + " no es un Email valido";

    if (fieldObject && fieldObject.value && fieldObject.value.length > 0)
    {
        if (/^\w+([\.-]?\w+)*@\w+([\.-]?\w+)*(\.\w{2,3})+$/.test(fieldObject.value))
        {
            _arq_insertValidationMsg(fieldId, msg);
        }
    }
}




