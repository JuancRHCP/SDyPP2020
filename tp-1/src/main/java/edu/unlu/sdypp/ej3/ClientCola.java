package edu.unlu.sdypp.ej3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

import org.json.*;

public class ClientCola {
	static String ip = "localhost";
	static int puerto = 9000; 
	static Scanner s = new Scanner(System.in);
	private int IdCliente;
	private Socket socket;

	
	public int getIdCliente() {
		return IdCliente;
	}

	public void setIdCliente(int idcli) {
		IdCliente = idcli ;
	}

	public ClientCola(int IdCliente, String ip, int puerto) throws UnknownHostException, IOException {
		this.socket = new Socket (ip, puerto);
		this.IdCliente = IdCliente;
	}
	
	public String readMessage() throws IOException {
		BufferedReader inputChannel = new BufferedReader (new InputStreamReader (socket.getInputStream()));
		return inputChannel.readLine();
	}
	
	public void writeMessage(int destinoid, String m) throws IOException {
		PrintWriter outputChannel = new PrintWriter (socket.getOutputStream(),true);
		String completeMessage = new String();
		completeMessage = String.valueOf(destinoid) + "|" + m ;
		outputChannel.println(completeMessage);
	}
	
	public void writeMessage(String m) throws IOException {
		PrintWriter outputChannel = new PrintWriter (socket.getOutputStream(),true);
		String completeMessage = new String();
		completeMessage = String.valueOf(this.IdCliente) + "|" + m ;
		outputChannel.println(completeMessage);
	}
	
	public void end() throws IOException {
		
	}
	
	public static void menuCli(int id) {
		System.out.println();
		System.out.println(" --- Bandeja de Mensajes del Cliente --- " + String.valueOf(id));
		System.out.println("send [id-client]\t\tEnviar mensaje -");
		System.out.println("read\t\t\tLeer mensajes -");
		System.out.println("help\t\t\tMuestra mensajes -");
		System.out.println("exit\t\t\tSalir -");
		System.out.println();
	}
	
	public static String[] splitArgs(String c) {
		c = c.trim();
		return c.split(" ");
	}
	
	public static Integer obtenerOpcion() {
		int op;
		System.out.print(" --- ");
		while ((!s.hasNextInt()) || (1 < s.nextInt() || s.nextInt() > 4)) {s.next();}
		op = s.nextInt();
		return op;
	}
	
	public static void interpretCmd(String line, ClientCola cliente) throws IOException, Exception
	{
		String[] args = splitArgs(line);
		String c = args[0];
		int idDestino;
		if (c.equals("enviar")) {
			if (cliente != null && args.length == 2) {
				idDestino = Integer.parseInt(args[1]);
				System.out.println(" --- Ingresar el Asunto del mensaje : --- ");
				String asunto = s.nextLine();
				System.out.println(" --- Ingresar el cuerpo del mensaje : --- ");
				String body = s.nextLine();
				String m = new JSONObject()
						.put("asunto", asunto)
						    .put("body", body).toString();
				if (m == null) System.err.println(" --- ERROR - Debe ingresar un mensaje --- ");
				cliente.writeMessage("ENVIAR");
				cliente.writeMessage(idDestino, m);
				System.out.println(" --- El Mensaje fue Enviado --- ");
			} else {
				System.err.println(" --- El Mensaje no fue Enviado ---");
			}
		} else if (c.equals("leer")) {
			if (cliente != null && args.length == 1) {
				cliente.writeMessage("LEER");
				String m2 = cliente.readMessage();
				int cant = 1;
				while(!m2.trim().equals(" --- No hay mensajes --- ")) {
					JSONObject jsonMsg = new JSONObject(m2);
					System.out.println("\t----- MENSAJE " + c + "-----");
					System.out.println("ASUNTO del mensaje :\n\t" + jsonMsg.get("asunto"));
					System.out.println("BODY del mensaje :\n\t" + jsonMsg.get("body"));
					cant++;
					m2 = cliente.readMessage();
				}
				if (cant > 1) {
					System.out.println(" --- No hay mas mensajes por leer ---");
				} else {
					System.out.println(" --- No hay mas mensajes por leer ---");
				}
			}
		} else if (c.equals("Ayuda")) {
				menuCli(cliente.getIdCliente());
		} else if (c.equals("Salir")) {
			cliente.end();
		} else {
			System.err.println("  --- Esta opción es inválida --- ");
		}
	}
		
	
	public static void main(String[] args) throws Exception {
		System.out.print(" --- Ingresar el número id del nuevo cliente: --- ");
		while ((!s.hasNextInt())) {s.next();}
		int ic = s.nextInt();
		ClientCola cliente = new ClientCola(ic , ip , puerto);
		s = new Scanner(System.in);
		System.out.println(" --- Ingresar - Ayuda - para poder observar las opciones ---");
		while (true){
			System.out.print(" --- ");
			interpretCmd(s.nextLine(), cliente);
		}
	}
}