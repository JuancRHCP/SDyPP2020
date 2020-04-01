package edu.unlu.sdypp.ej1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class Server {
	int port;
	
	public Server (int p) {
		this.port = p;
		this.startServer();
		
	}
	private void startServer() {
		try {
			ServerSocket ss = new ServerSocket (this.port);	
			boolean s=false;
			while (!s) {
				System.out.println("El Servidor fue iniciado en: "+this.port+ ". ----");
				Socket client = ss.accept();
				System.out.println("Se conecto el cliente- "+client.getInetAddress().getCanonicalHostName()+" : "+client.getPort());

				BufferedReader inputChannel = new BufferedReader (new InputStreamReader (client.getInputStream()));
				PrintWriter outputChannel = new PrintWriter (client.getOutputStream(),true);
			
				String msg = inputChannel.readLine();
				System.out.println("El cliente ha enviado-  "+ msg);
				msg= "'"+msg+"'"+" , soy Servidor.";
				outputChannel.println(msg);
				System.out.println("El servidor ha respondido--");
				client.close();
				System.out.println("----Se ha cerrado la conexion con el cliente----");
				System.out.println("****************************************************");
				System.out.println("");
				
				int op;
				
				do{
					System.out.println("Que desea hacer:");
					System.out.println("1. Seguir escuchando");
					System.out.println("2. Terminar");
					Scanner scanner = new Scanner(System.in);
					op =  scanner.nextInt();
					
					switch (op) {
						case 1:
							s=false;
							break;
						case 0:
							s=true;
							System.out.println("-----Programa Terminado-----");
							break;
						default: 
							System.out.println("Opcion invalida");
							break;
					}
				}while ((op!=0)&&(op!=1));
			}
			
		} catch (IOException e) {
			System.out.println(" ERROR-- puerto en uso..");
		} 
	}
	
	public static void main(String[] args) {
		Server server = new Server(9000);
	}

}