package edu.unlu.sdypp.ej4;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import javax.net.ssl.SSLContext;

public class Server3y4 {
	int puerto;
	private ArrayList<String> mensajes;
	
	public Server3y4(int i) {
		this.puerto = i;
		this.mensajes =  new ArrayList<String>();
		this.startServer();
	}
	
	private void startServer() {
		
		try {
			ServerSocket ss = new ServerSocket (this.puerto);
			System.out.println("Server started on port: " + this.puerto);
			while (true) {
				Socket client = ss.accept();
				System.out.println(" --- El cliente fue conectado en el puerto --- "+client.getInetAddress().getCanonicalHostName()+" : "+client.getPort());
				ThreadS4 ts = new ThreadS4(client,  this.mensajes);
				Thread tsThread = new Thread (ts);
				tsThread.start();
			}
		} catch (IOException excp) {
			System.out.println(" ---  El puerto se encuentra en uso --- ");
		}
	}
	public static void main(String[] args) {
		Server3y4 s = new Server3y4(9000);

	}

}