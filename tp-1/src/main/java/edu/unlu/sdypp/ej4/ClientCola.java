package edu.unlu.sdypp.ej4;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

import org.json.JSONObject;
import org.json.JSONException;


public class ClientCola {
	static String IP = "localhost";
	static int puerto = 9000; 
	static Scanner scanner = new Scanner(System.in);
	private int IdCliente;
	private Socket s;

	public int getIdCliente() {
		return IdCliente;
	}

	public void setIdCliente(int idcliente) {
		IdCliente = idcliente;
	}

	public ClientCola(int IdCliente, String ip, int puerto) throws UnknownHostException, IOException {
		this.s = new Socket (ip, puerto);
		this.IdCliente = IdCliente;
	}
	
	public String readMessage() throws IOException {
		BufferedReader inputChannel = new BufferedReader (new InputStreamReader (s.getInputStream()));
		return inputChannel.readLine();
	}
	
	public void writeMessage(int DestinationId, String m) throws IOException {
		PrintWriter outputChannel = new PrintWriter (s.getOutputStream(),true);
		String completeMsg = new String();
		completeMsg = String.valueOf(DestinationId) + "|" + m ;
		outputChannel.println(completeMsg);
	}
	
	public void writeMessage(String m) throws IOException {
		PrintWriter outputChannel = new PrintWriter (s.getOutputStream(),true);
		String completeMsg = new String();
		completeMsg = String.valueOf(this.IdCliente) + "|" + m ;
		outputChannel.println(completeMsg);
	}
	
	public static void menuCliente(int numid) {
		System.out.println();
		System.out.println("Bandeja de Mensajes -> " + String.valueOf(numid));
		System.out.println("send [id-client]\t\tEnviar un mensaje: --");
		System.out.println("read\t\t\tLeer mensaje: ");
		System.out.println("help\t\t\tMostrar este mensaje: ");
		System.out.println("exit\t\t\tSalir");
		System.out.println();
	}
	
	public void end() throws IOException {
		
	}
	
	public static String[] splitArgs(String c) {
		c = c.trim();
		return c.split(" ");
	}
	
	public static void interpretCmd(String line, ClientCola cliente) throws IOException, JSONException {
		String[] args = splitArgs(line);
		String c = args[0];
		int idDestino;
		if (c.equals("enviar")) {
			if (cliente != null && args.length == 2) {
				idDestino = Integer.parseInt(args[1]);
				System.out.println(" --- Ingresar el asunto del mensaje ---" );
				String asunto = scanner.nextLine();
				System.out.println(" --- Ingresar el cuerpo del mensaje ---");
				String body = scanner.nextLine();
				String msg = new JSONObject().put("asunto", asunto).put("body", body).toString();
				if (msg == null) System.err.println(" --- ERROR - Se debe ingresar un mensaje --- ");
				cliente.writeMessage("ENVIAR");
				cliente.writeMessage(idDestino, msg);
				System.out.println(" --- El mensaje fue Enviado --- ");
			} else {
				System.err.println(" --- El mensaje no fue enviado --- ");
			}
		} else if (c.equals("leer")) {
			if (cliente != null && args.length == 1) {
				cliente.writeMessage("LEER");
				String msgX = cliente.readMessage();
				int cant = 1;
				while(!msgX.trim().equals("No hay más mensajes")) {
					JSONObject jsonMsg = new JSONObject(msgX);
					System.out.println("\t----- MENSAJE " + c + "-----");
					System.out.println("ASUNTO:\n\t" + jsonMsg.get("asunto"));
					System.out.println("BODY:\n\t" + jsonMsg.get("body"));
					System.out.println("\n\t> Desea marcarlo como leido? Y/N");
					String opt = scanner.nextLine();
					if (opt.equals("Y") || opt.equals("y")) {
						cliente.writeMessage("ACK");
					} else {
						cliente.writeMessage("NACK");
					}
					msgX = cliente.readMessage();
					cant++;
				}
				if (cant > 1) {
					System.out.println(" --- No hay mas mensajes --- ");
				} else {
					System.out.println(" --- No hay mensajes --- ");
				}
			}
		} else if (c.equals("help")) {
				menuCliente(cliente.getIdCliente());
		} else if (c.equals("exit")) {
			cliente.end();
		} else {
			System.err.println(" --- La opción ingresada es incorrecta ---");
		}
	}
		
	public static Integer obtenerOpcion() {
		int op;
		System.out.print(" --- ");
		while ((!scanner.hasNextInt()) || (1 < scanner.nextInt() || scanner.nextInt() > 4)) {scanner.next();}
		op = scanner.nextInt();
		return op;
	}

	
	public static void main(String[] args) throws IOException, JSONException {
		System.out.print("--- Ingresar el número ID del nuevo cliente --- ");
		while ((!scanner.hasNextInt())) {scanner.next();}
		int cc = scanner.nextInt();
		ClientCola cliente = new ClientCola(cc, IP, puerto);
		scanner = new Scanner(System.in);
		System.out.println(" --- Ingresar - help - para ver las opciones --- ");
		while(true) {
			System.out.print(" --- ");
			interpretCmd(scanner.nextLine(), cliente);
		}
	}
}