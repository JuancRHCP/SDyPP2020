package edu.unlu.sdypp.ej2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ThreadS implements Runnable{
	Socket cliente;
	int tiempo;
	
	public ThreadS (Socket cliente, int tiempo) {
		this.cliente = cliente;
		this.tiempo = tiempo;
	}
	
	public void run() {
		try {
			BufferedReader inputChannel = new BufferedReader (new InputStreamReader (cliente.getInputStream()));
			PrintWriter outputChannel = new PrintWriter (cliente.getOutputStream(),true);
			String m = inputChannel.readLine();
			System.out.println(" --- El cliente envio: ---"+ m);
			m= "'"+m+"'"+" . [from Server]";
			outputChannel.println(m);
			System.out.println(" --- La respuesta ha sido enviada ---");
			try {
				Thread.sleep(this.tiempo);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			cliente.close();
			System.out.println("--- Se ha cerrado una conexion ---");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

}
