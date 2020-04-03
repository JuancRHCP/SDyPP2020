package edu.unlu.sdypp.ej2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;


public class Server {
	int puerto;
	int tiempo;
	
	public Server (int i, int s) {
		this.puerto = i;
		this.tiempo= s;
		this.startServer();
		
	}
	
	//Doy aviso en que puerto se inicio el Servidor 
	//Informo el cliente conectado
	private void startServer() {
		try {
			ServerSocket ss = new ServerSocket (this.puerto);
			System.out.println("---- El Servidor fue iniciado en el puerto : "+this.puerto+ ". ----");
			while (true) {
				Socket client = ss.accept();
				System.out.println("-- Se conecto el cliente: -- "+client.getInetAddress().getCanonicalHostName()+" : "+client.getPort());
				ThreadS ts = new ThreadS(client, this.tiempo);
				Thread tsThread = new Thread (ts);
				tsThread.start();
			}	
		} catch (IOException e) {
			System.out.println(" -- El puerto está en uso --");
		} 
	}
	
	
	public static void main(String[] args) {
		System.out.println(" --- Ingrese el puerto a escuchar: ---");
		Scanner s = new Scanner(System.in);
		int puerto =  s.nextInt();
		System.out.println(" --- Ingrese tiempo de Sleep -Ingrese 0 si no lo desea- : ---");
		s = new Scanner(System.in);
		int tiempo=  s.nextInt()*1000;
		Server servidor = new Server(puerto, tiempo);
	}

}



