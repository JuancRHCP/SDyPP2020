package edu.unlu.sdypp.ej3;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server3y4 {
	int puerto;
	private ArrayList<String> Colademensaje;
	
	public Server3y4 (int i) {
		this.puerto = i;
		this.Colademensaje =  new ArrayList<String>();
		this.startServer();
	}
	
	private void startServer() {
		try {
			ServerSocket ssocket = new ServerSocket (this.puerto);
			System.out.println(" --- El servidor está en el puerto: --- " + this.puerto);
			while (true) {
				Socket cliente = ssocket.accept();
				System.out.println("-- El cliente se conecto en : "+cliente.getInetAddress().getCanonicalHostName()+" : "+cliente.getPort());
				Thread3 ts = new Thread3(cliente,  this.Colademensaje);
				Thread tsThread = new Thread (ts);
				tsThread.start();
			}
		} catch (IOException e) {
			System.out.println(" --- El puerto está en uso ---");
		}
	}
	public static void main(String[] args) {
		Server3y4 stcp = new Server3y4(9000); 
		//Puerto 9000

	}

}