package edu.unlu.sdypp.ej2.withsyncronize;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import org.slf4j.Logger;

import com.google.gson.Gson;

public class ExtraccionThread implements Runnable {
	private String filename;
	private Logger log;
	private Socket client;
	private Gson gson;

	ExtraccionThread(String filename, Logger log, Gson gson, Socket client){
		this.filename = filename;
		this.log = log;
		this.client = client;
		this.gson = gson;
	}

	public void run() {
		try {

			BufferedReader inputChannel = new BufferedReader (new InputStreamReader (this.client.getInputStream()));
			PrintWriter outputChannel = new PrintWriter (this.client.getOutputStream(),true);
			log.info(" --- Esperando la extracción ---");
			String str;
			while((str = inputChannel.readLine()) != null) {
				Double monto = gson.fromJson(str, Double.class);
				log.info("  --- Se ha realizado una nueva extraccion por $" + monto + " --- ");
				BufferedReader br = new BufferedReader(new FileReader(filename));;
				synchronized (br) {
					Double saldo = new Double(br.readLine());
					log.info(" --- Previo a la Extraccion, el Monto era de : " + monto + ", Saldo:" + saldo  + " --- ");
					if (saldo >= monto) {
						saldo -= monto;
						try {
							Thread.sleep(80);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						FileWriter writer = new FileWriter(filename);
						writer.write(String.valueOf(saldo), 0, String.valueOf(saldo).length());
						String json = gson.toJson("--- La Transaccion ha sido Exitosa ---  El saldo actual es:  "+saldo);
						outputChannel.print(json);
						log.info("  --- La Extraccion ha sido Exitosa --- ");
						writer.close();
					} else {
						String json = gson.toJson("--- La transaccion ha sido rechazada --- El saldo actual es : "+saldo);
						outputChannel.print(json);
						log.info("--- La extraccion ha sido rechazada --- El Saldo es insuficiente --- ");
					}
					log.info(" --- Luego de la Extraccion, el monto era de :" + monto + ", Saldo:" + saldo + " --- ");
					log.info("--- Esperando extracción ---");
					br.close();
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
