package edu.unlu.sdypp.ej1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Client {
		
		public Client(String ip, int port) {
			try {
				Socket s = new Socket (ip, port);
				System.out.println("-----Se ha iniciado el Cliente----");
				BufferedReader inputChannel = new BufferedReader (new InputStreamReader (s.getInputStream()));
				PrintWriter outputChannel = new PrintWriter (s.getOutputStream(),true);
				
				System.out.println("Ingrese un mensaje- ");
				Scanner scannerMSJ = new Scanner(System.in);
				String msj =  scannerMSJ.nextLine(); 
				outputChannel.println(msj);
				System.out.println ("----Su mensaje ha sido enviado----");
				String response = inputChannel.readLine();
				
				System.out.println("Respuesta del Servidor- "+response);
				s.close();
				
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		public static void main(String[] args) {
			//BasicConfigurator.configure();
	        boolean esIP = false;
	        Scanner scanner;
	        String ip="";
	        
			 while (!esIP) { 
				System.out.println("Ingrese Servidor-");
				scanner = new Scanner(System.in);
				ip =  scanner.nextLine(); 
				 esIP= validacion(ip);
				 if (!esIP) {System.out.println("Lo que ingreso no es una IP valida");}
			 }
			System.out.println("Ingrese el puerto- ");
			scanner = new Scanner(System.in);
			int port =  scanner.nextInt(); 
			Client ctcp = new Client (ip, port);

		}

		public static boolean validacion(String ip) {
			Scanner scanner;
			if (ip.equals("localhost")) { 
				return true;
			}else {
				String [] sNumeros=ip.split("\\.");
	        	if (sNumeros.length <4 || sNumeros.length >4) {
	        		  return false;
	        		 }else return true;
			}
		}

	}

