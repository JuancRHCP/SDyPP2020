package edu.unlu.sdypp.ej4;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.Array;
import java.util.ArrayList;

public class ThreadS4 implements Runnable{
	Socket cliente;
	private ArrayList<String> mensajes;
	
	public ThreadS4 (Socket cliente, ArrayList<String> mensajes) {
		this.cliente = cliente;
		this.mensajes =  mensajes;
	}
	
	public void run() {	
		try {
			ArrayList<String> Eliminar = new ArrayList<String>();
			BufferedReader inputChannel = new BufferedReader (new InputStreamReader (this.cliente.getInputStream()));
			PrintWriter outputChannel = new PrintWriter (this.cliente.getOutputStream(),true);
			while(!cliente.isClosed()) {
				String opt = inputChannel.readLine();
				if (opt.substring(opt.indexOf("|")+1, opt.length()).equals("ENVIAR")) {
					mensajes.add(inputChannel.readLine());
				} else if (opt.substring(opt.indexOf("|")+1, opt.length()).equals("LEER")) {
					String srcId = opt.substring(0, opt.indexOf("|"));
					
					if (!this.mensajes.isEmpty()) {
						for (String s : mensajes) {
							String IdDestino = s.substring(0, s.indexOf("|"));
							String sendMessage = s.substring(s.indexOf("|")+1, s.length());
							if (IdDestino.equals(srcId)) {
								outputChannel.println(sendMessage);
								String ack = inputChannel.readLine();
								if (ack.substring(ack.indexOf("|")+1, ack.length()).compareTo("ACK") == 0) {
									Eliminar.add(s);
								}
							}
						}
					}
					outputChannel.println(" --- No hay mensaje --- ");	
				}
				for (String string : Eliminar) {
						mensajes.remove(string);
				}
			}
			this.cliente.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	

}