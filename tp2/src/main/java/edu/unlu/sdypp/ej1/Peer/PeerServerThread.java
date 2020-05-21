package edu.unlu.sdypp.ej1.Peer;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Set;

import org.slf4j.Logger;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import edu.unlu.sdypp.ej1.FilesP;
import edu.unlu.sdypp.ej1.Message;
import edu.unlu.sdypp.ej1.Seeder;
import edu.unlu.sdypp.ej1.StoreFile;

public class PeerServerThread implements Runnable {

	private Logger log;
	private Gson gson;
	private Socket client;
	private String LOCAL_FILES_INFO;


	public PeerServerThread(Logger log, Gson gson, Socket client, String LOCAL_FILES_INFO){
		this.log = log;
		this.gson = gson;
		this.client = client;
		this.LOCAL_FILES_INFO = LOCAL_FILES_INFO;
	}

	@Override
	public void run() {
		try {
			BufferedReader inputC = new BufferedReader (new InputStreamReader (client.getInputStream()));
			PrintWriter outputC = new PrintWriter (client.getOutputStream(),true);
			String msg;
			int index;
			InputStream in = null;
			while((msg = inputC.readLine()) != null) {
				Message decodedMsg = gson.fromJson(msg, Message.class);
				if (decodedMsg.getConsole().equals("get-file")) {
					StoreFile sf = findFile(decodedMsg);
					if (sf != null) {
						Message m = new Message("peer-data");
						m.setParametro("status", "OK");
						m.setParametro("checksum", sf.getChecksum());
						m.setParametro("Nombre", sf.getName());
						String json = gson.toJson(m);
						outputC.println(json);
						sendFile(sf);
					} else {
						Message m = new Message("peer-error");
						m.setParametro("status", "FAILED - File Not Found!");
						m.setParametro("descrip", "File Not Found!");
						String json = gson.toJson(m);
						outputC.println(json);
					}

				}
			}
			client.close();
		}  catch (SocketException s) {
			//log.info(" [!] - Client disconnected.");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void sendFile(StoreFile sf) throws IOException {
		OutputStream out = client.getOutputStream();
		FileInputStream in = null;
		if (sf != null) {
			File file = new File(sf.getPathname());
			// Get the size of the file
			long length = file.length();
			byte[] bytes = new byte[16 * 1024];
			in = new FileInputStream(file);
			int count;
			while ((count = in.read(bytes)) > 0) {
				out.write(bytes, 0, count);
			}
		}
		out.close();
		in.close();
	}

	private StoreFile findFile(Message m) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(LOCAL_FILES_INFO));
		ArrayList<StoreFile> fp = gson.fromJson(br, new TypeToken<ArrayList<StoreFile>>(){}.getType());
		StoreFile p = null;
		String nombre = m.getParametro("name");
		String chk = m.getParametro("checksum");
		if (nombre != null && chk != null) {
			for (StoreFile fap : fp) {
				if (fap.getName().equals(nombre) && fap.getChecksum().equals(chk)) {
					p = fap;
				}
			}
		} else if (nombre != null) {
			for (StoreFile fap : fp) {
				if (fap.getName().equals(nombre)) {
					p = fap;
				}
			}
		} else if (chk != null) {
			for (StoreFile fap : fp) {
				if (fap.getChecksum().equals(chk)) {
					p = fap;
				}
			}
		}
		br.close();
		return p;
	}

}