package edu.unlu.sdypp.ej1.Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Logger;

import org.slf4j.LoggerFactory;


import com.google.gson.Gson;

public class ServerMain {
	private final org.slf4j.Logger log =  LoggerFactory.getLogger(ServerMain.class);
	private static final String PEERS_INFO="src/main/java/edu/unlu/sdypp/ej1/Server/Resources/peers-info.json";
	private static final String FILES_INFO="src/main/java/edu/unlu/sdypp/ej1/Server/Resources/files-info.json";
	private Gson gson;
	private int p;
	
	public ServerMain(int p) {
		super();
		this.p = p;
		this.gson = new Gson();
	}

	public void startServer() {
		try {
			ServerSocket ssock = new ServerSocket (this.p);
			log.info(" --- El Servidor se inición en : " + this.p + " --- ");
			boolean flag = true;
			while (flag) {
				Socket client = ssock.accept();
				log.info(" --- El Cliente se conecto " + client.getInetAddress().getCanonicalHostName()+":"+client.getPort() +  " --- ");
				ServerThread ts = new ServerThread((org.slf4j.Logger) log, gson, client, PEERS_INFO, FILES_INFO);
				Thread tsThread = new Thread(ts);
				tsThread.start();
			}
			ssock.close();
		} catch (IOException e) {
			log.info(" --- El Puerto está en uso --- ");
		}
	}

	public static void main(String[] args) {
		int thread = (int) Thread.currentThread().getId();
		String pName = ServerMain.class.getSimpleName().toString()+"-"+thread;
		System.setProperty("log.name",pName);
		ServerMain ss = new ServerMain(8090);
		ss.startServer();
	}
}