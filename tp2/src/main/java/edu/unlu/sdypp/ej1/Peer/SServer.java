package edu.unlu.sdypp.ej1.Peer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

public class SServer implements Runnable{
	private final Logger log = LoggerFactory.getLogger(SServer.class);
	private static final String FILES_INFO="src/main/java/edu/unlu/sdypp/ej1/Peer/resources/local-files-info.json";
	private Gson gson;
	private int p;
	private ServerSocket ssock;

	public SServer(int p) {
		super();
		this.p = p;
		this.gson = new Gson();
	}

	public void startServer() {
		try {
			ssock = new ServerSocket (this.p);
			while (true) {
				Socket client = ssock.accept();
				PeerServerThread ts = new PeerServerThread(log, gson, client, FILES_INFO);
				Thread tsThread = new Thread(ts);
				tsThread.start();
			}
		} catch (IOException e) {
		}
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		startServer();
	}
}