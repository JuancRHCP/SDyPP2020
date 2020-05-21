package edu.unlu.sdypp.ej1;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Message implements Serializable {

	private static final long serialVersionUID = 1L;
	private String cmd;
	private Map<String, String> parametros;
	private byte[] b; //Cuerpo del Mensaje
	
	public Message(String cmd) {
		super();
		this.parametros = new HashMap<String,String>();
		this.setConsole(cmd);
		this.b = null;
	}
	
	public void setParametro(String key, String value) {
		this.parametros.put(key, value);
		
	}
	
	public String getParametro(String key) {
		return this.parametros.get(key);
	}
	
	public void delParametro(String key) {
		this.parametros.remove(key);
	}

	public byte[] getBody() {
		return b;
	}

	public void setBody(byte[] b) {
		this.b = b;
	}

	public String getConsole() {
		return cmd;
	}

	public void setConsole(String cmd) {
		this.cmd = cmd;
	}
	
}
