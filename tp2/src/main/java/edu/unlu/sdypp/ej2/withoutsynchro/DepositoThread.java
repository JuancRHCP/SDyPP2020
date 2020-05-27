package edu.unlu.sdypp.ej2.withoutsynchro;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import org.slf4j.Logger;

import com.google.gson.Gson;

public class DepositoThread implements Runnable {
	private String filename;
	private Logger log;
	private Socket client;
	private Gson gson;

	DepositoThread(String filename, Logger log, Gson gson, Socket client){
		this.filename = filename;
		this.log = log;
		this.client = client;
		this.gson = gson;
	}

	@Override
	public void run() {
		try {

			BufferedReader inputChannel = new BufferedReader (new InputStreamReader (this.client.getInputStream()));
			PrintWriter outputChannel = new PrintWriter (this.client.getOutputStream(),true);
			log.info("--- Esperando Depósito ---");
			String k;
			while((k = inputChannel.readLine()) != null) {
				Double monto = gson.fromJson(k, Double.class);
				log.info("  --- Nuevo Deposito por $" + monto + " --- " );
				BufferedReader br;
				br = new BufferedReader(new FileReader(filename));
				Double saldo = new Double(br.readLine());
				log.info("--- Previo al Deposito, el Monto era de :" + monto + ", Saldo:" + saldo + " --- ");
				saldo += monto;
				try {
					Thread.sleep(40);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				FileWriter writer = new FileWriter(filename);
				writer.write(String.valueOf(saldo), 0, String.valueOf(saldo).length());
				String json = gson.toJson("--- La Transaccion ha sido Exitosa ---  El saldo actual es:  "+saldo);
				outputChannel.print(json);
				log.info("--- El Deposito ha sido Exitoso ---");
				writer.close();
				log.info(" --- Luego de la Extraccion, el monto era de :" + monto + ", Saldo:" + saldo + " --- ");
				br.close();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
