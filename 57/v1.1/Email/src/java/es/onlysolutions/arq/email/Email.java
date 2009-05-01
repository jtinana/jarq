package es.onlysolutions.arq.email;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Clase abstracta de todos los email. Se obtiene a trav�s de una factor�a y su env�o se realiza en
 * funci�n de la implementacion obtenida de la factor�a.
 */
public abstract class Email {
	//Lista de direcciones a a�adir en el TO del email.
	private List<String> tos= new ArrayList<String>(10);
	//Lista de direcciones a a�adir en copia.
	private List<String> ccs= new ArrayList<String>(10);
	//Lista de direcciones a a�adir en copia oculta.
	private List<String> bcs= new ArrayList<String>(10);
	//Lista para almacenar los ficheros adjuntos a enviar.
	private List<File> attachments= new ArrayList<File>(5);
	//Lista para ficheros a a�adir inline.
	private Map<String, File> inLines= new HashMap<String, File>(5);
	//Texto del mensaje a poner en el body del correo.
	private String text;
	//Direccion a poner en el from de los correos.
	private String from;
	//Texto a indicar el el subject del correo.
	private String subject;


	/**
	 * Getter for property 'subject'.
	 * 
	 * @return Value for property 'subject'.
	 * @see #subject
	 */
	public String getSubject() {
		return subject;
	}


	/**
	 * Setter for property 'subject'.
	 * 
	 * @param subject Value to set for property 'subject'.
	 * @see #subject
	 */
	public void setSubject(String subject) {
		this.subject= subject;
	}


	/**
	 * Getter for property 'from'.
	 * 
	 * @return Value for property 'from'.
	 * @see #from
	 */
	public String getFrom() {
		return from;
	}


	/**
	 * Setter for property 'from'.
	 * 
	 * @param from Value to set for property 'from'.
	 * @see #from
	 */
	public void setFrom(String from) {
		this.from= from;
	}


	/**
	 * Getter for property 'text'.
	 * 
	 * @return Value for property 'text'.
	 * @see #text
	 */
	public String getText() {
		return text;
	}


	/**
	 * Getter for property 'tos'.
	 * 
	 * @return Value for property 'tos'.
	 * @see #tos
	 */
	public List<String> getTos() {
		return tos;
	}


	/**
	 * Getter for property 'ccs'.
	 * 
	 * @return Value for property 'ccs'.
	 * @see #ccs
	 */
	public List<String> getCcs() {
		return ccs;
	}


	/**
	 * Getter for property 'bcs'.
	 * 
	 * @return Value for property 'bcs'.
	 * @see #bcs
	 */
	public List<String> getBcs() {
		return bcs;
	}


	/**
	 * Setter for property 'text'.
	 * 
	 * @param text Value to set for property 'text'.
	 * @see #text
	 */
	public void setText(String text) {
		this.text= text;
	}


	/**
	 * A�ade una o varias direcciones separadas por punto y coma de to a la lista de TOs.
	 * 
	 * @param to La cadena con la direcci�n o direcciones a a�adir.
	 */
	public void addTo(String to) {
		if (to.indexOf(';') == -1) {
			tos.add(to);
		} else {
			StringTokenizer st= new StringTokenizer(to, ";");
			while (st.hasMoreTokens()) {
				tos.add(st.nextToken());
			}
		}
	}


	/**
	 * A�ade una o varias direcciones separadas por punto y coma en copia al email.
	 * 
	 * @param ccc La cadena con la direcci�n o direcciones a a�adir.
	 */
	public void addCcc(String ccc) {
		if (ccc.indexOf(';') == -1) {
			ccs.add(ccc);
		} else {
			StringTokenizer st= new StringTokenizer(ccc, ";");
			while (st.hasMoreTokens()) {
				ccs.add(st.nextToken());
			}
		}
	}


	/**
	 * A�ade una o varias direcciones separadas por punto y coma a la lista de copias ocultas.
	 * 
	 * @param bcc La cadena con la direcci�n o direcciones a a�adir en copia oculta.
	 */
	public void addBcc(String bcc) {
		if (bcc.indexOf(';') == -1) {
			bcs.add(bcc);
		} else {
			StringTokenizer st= new StringTokenizer(bcc, ";");
			while (st.hasMoreTokens()) {
				bcs.add(st.nextToken());
			}
		}
	}


	/**
	 * A�ade un adjunto al correo. El nombre del fichero ser� el nombre bajo el que se adjuntar� al
	 * correo.
	 * 
	 * @param file el fichero a enviar como adjunto.
	 */
	public void addAttachment(File file) {
		attachments.add(file);
	}


	/**
	 * A�ade un fichero inline al correo. Es decir, ir� junto con el correo y no como adjunto. Para
	 * ello se debe indicar el cid adecuado que corresponder� con el fichero en el c�digo HTML del
	 * body.
	 * 
	 * @param cid El cid HTML que corresponde con el fichero adjunto.
	 * @param inLineFile El fichero adjunto mediante cid con el HTML.
	 */
	public void addInLine(String cid, File inLineFile) {
		inLines.put(cid, inLineFile);
	}


	/**
	 * Getter for property 'attachments'.
	 * 
	 * @return Value for property 'attachments'.
	 * @see #attachments
	 */
	public List<File> getAttachments() {
		return attachments;
	}


	/**
	 * Getter for property 'inLines'.
	 * 
	 * @return Value for property 'inLines'.
	 * @see #inLines
	 */
	public Map<String, File> getInLines() {
		return inLines;
	}


	/**
	 * Limpia el mensaje para poder reutilizar varias veces la misma clase.
	 */
	public void cleanAll() {
		tos.clear();
		bcs.clear();
		ccs.clear();
		attachments.clear();
		inLines.clear();
		subject= null;
		text= null;
		from= null;
	}


	/**
	 * M�todo abstracto para enviar el contenido recogido en el correo. Depender� de la
	 * implementaci�n concreta.
	 */
	public abstract void send();

}
