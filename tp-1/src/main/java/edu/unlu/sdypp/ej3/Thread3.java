package edu.unlu.sdypp.ej3;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.Array;
import java.util.ArrayList;

public class Thread3 implements Runnable{
	Socket cliente;
	private ArrayList<String> Colademensaje;
	
	
	public Thread3 (Socket cliente, ArrayList<String> Colademensaje) {
		this.cliente = cliente;
		this.Colademensaje =  Colademensaje;
	}
	
	public void run() {	
		try {
			BufferedReader inputChannel = new BufferedReader (new InputStreamReader (this.cliente.getInputStream()));
			PrintWriter outputChannel = new PrintWriter (this.cliente.getOutputStream(),true);
			while(!cliente.isClosed()) {
				String op = inputChannel.readLine();
				if (op.substring(op.indexOf("|")+1, op.length()).equals("ENVIAR")) {
					Colademensaje.add(inputChannel.readLine());
				} else if (op.substring(op.indexOf("|")+1, op.length()).equals("LEER")) {
					String ingid = op.substring(0, op.indexOf("|"));
					if (!this.Colademensaje.isEmpty()) {
						for (String s : Colademensaje) {
							String destinoId = s.substring(0, s.indexOf("|"));
							String sendMessage = s.substring(s.indexOf("|")+1, s.length());
							if (destinoId.equals(ingid)) {
								outputChannel.println(sendMessage);
							}
						}
					}
					outputChannel.println(" ---  No hay mensajes ---");	
				}
			}
			this.cliente.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	

}