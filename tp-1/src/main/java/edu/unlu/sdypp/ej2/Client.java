package edu.unlu.sdypp.ej2;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;
		
public class Client {

	//Inicio el Cliente con un número Random y envio un mensaje al servidor.
	public Client (String ip, int puerto) {
			try {
				int NumeroCliente = (int)(Math.random() * 1000) + 1; 
				String m;
				Socket s = new Socket (ip, puerto);
				System.out.println("----- El Cliente : "+NumeroCliente+" fue iniciado----");
				BufferedReader inputChannel = new BufferedReader (new InputStreamReader (s.getInputStream()));
				PrintWriter outputChannel = new PrintWriter (s.getOutputStream(),true);
				System.out.println("Ingrese un mensaje: ");
				Scanner scannerm = new Scanner(System.in);
				m =  scannerm.nextLine();
				m+= ".--- (Usuario Aleatorio Nº: "+NumeroCliente +"---"; 
				outputChannel.println(m);
				outputChannel.println ("--- El mensaje fue enviado al Servidor ---");
				String response = inputChannel.readLine();
				System.out.println("- El servidor respondio:  "+response);
				s.close();
				
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		
	//Verifico el ingreso del servidor (utilizando una IP Valida) y un número de Puerto
		public static void main(String[] args) {
	        boolean bip= false;
	        Scanner s;
	        String ip="";
	        int puerto;
	        
			while (!bip) { 
				System.out.println("--- Ingrese el servidor: --- ");
				s = new Scanner(System.in);
				ip =  s.nextLine(); 
				 bip= validarIP(ip);
				 if (!bip) {System.out.println(" --- Lo que ha ingresado no es una IP valida ---");}
			 }
			System.out.println("Ingrese el número de Puerto: ");
			s= new Scanner(System.in);
			puerto =  s.nextInt();    //port=9000; ip= "localhost"; para probar
			Client c = new Client (ip, puerto);

		}
		
		//Para validar el ingreso de la IP 
		public static boolean validarIP(String ip) {
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
