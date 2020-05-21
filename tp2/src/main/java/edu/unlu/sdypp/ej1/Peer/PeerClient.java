package edu.unlu.sdypp.ej1.Peer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.lang.reflect.Type;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.NoSuchFileException;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import edu.unlu.sdypp.ej1.PException;
import edu.unlu.sdypp.ej1.Main;
import edu.unlu.sdypp.ej1.Message;
import edu.unlu.sdypp.ej1.Seeder;
import edu.unlu.sdypp.ej1.StoreFile;

public class PeerClient implements Runnable{
	private final Logger log = LoggerFactory.getLogger(PeerClient.class);
	private static String MASTER_INFO = "src/main/java/edu/unlu/sdypp/ej1/Peer/resource/master-info.json";
	private static String LOCAL_FILES_INFO = "src/main/java/edu/unlu/sdypp/ej1/Peer/resource/local-files-info.json";
	private Gson gson = new Gson();
	private Set<Main> mains;
	private Set<StoreFile> storeFiles;
	private Main m;
	private BufferedReader inputC;
	private PrintWriter outputC;
	private Socket sockMain;
	private Iterator<Main> iteratorMain;
    static Scanner scanner = new Scanner(System.in);
	private static final Type SET_MAIN_TYPE = new TypeToken<Set<Main>>(){}.getType();
	private static final Type SET_STOREFILE_TYPE = new TypeToken<Set<StoreFile>>(){}.getType();
	private static final int TRYS_IN_PEER = 3;
	private static final long RETRY_SLEEP_INTERVAL = 3000;
	private int pPServer;
	private String ipPServer;


	PeerClient(int pPServer, String ipPServer){
		try {
			this.pPServer = pPServer;
			this.ipPServer = ipPServer;
			JsonReader rr;
			rr = new JsonReader(new FileReader(MASTER_INFO));
			mains = null;
			if (rr != null) {
				mains = gson.fromJson(rr, SET_MAIN_TYPE);
			}
			rr = new JsonReader(new FileReader(LOCAL_FILES_INFO));
			if (rr != null) {
				storeFiles = gson.fromJson(rr, SET_STOREFILE_TYPE);
			}
			this.iteratorMain = mains.iterator();
			connectToMain();
			rr.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			System.out.println(" --- El archivo no existe ---");
		} catch (JsonIOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonSyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (PException e) {
			// TODO Auto-generated catch block
			System.out.println(" --- "+e.getMessage() + " --- ");
		}
	}
	
	private void connectToMain() throws PException {
		try {
			this.m = getNextMain();
			configurationMaster();
		} catch (ConnectException | UnknownHostException e){
			//System.out.println(" --- El master "+ m.getIp() +" : "+ m.getPort() +" está caido ---");
		} catch (IOException e) {
			System.out.println(" --- ERROR - al conectarse con el Master "+ m.getIp() +" : "+ m.getPort());
		}
		if (this.m == null) throw new PException(" --- No hay ningún Master disponible --- ");
	}

	private Main getNextMain() {
		if (!iteratorMain.hasNext())
			return null;
		return iteratorMain.next();
	}


	private void configurationMaster() throws UnknownHostException, IOException, ConnectException {
		this.sockMain = new Socket(this.m.getIp(), this.m.getPort());
		this.inputC = new BufferedReader (new InputStreamReader (sockMain.getInputStream()));
		this.outputC = new PrintWriter (sockMain.getOutputStream(),true);
	}

	private String extractFileChecksums(Set<StoreFile> sf) {
		String s = "";
		sf = null;
		sf = new HashSet <>();
		if (!sf.isEmpty()) {
			for (StoreFile stf : sf) {
				s += stf.getChecksum() +",";
			}
			s = s.substring(0, s.length()-1);
		}
		return s;
	}

	private String extractFileNames(Set<StoreFile> sf) {
		String s = "";
		sf =  (Set<StoreFile>) ((sf == null)?new HashSet<>(): sf);
		if (!sf.isEmpty()) {
			for (StoreFile stf : sf) {
				s += stf.getName() +",";
			}
			s = s.substring(0, s.length()-1);
		}
		return s;
	}


	public void addFile(StoreFile f) {
		JsonReader jr;
		Set<StoreFile> fp = new HashSet<>();
		try {
			jr = new JsonReader(new FileReader(LOCAL_FILES_INFO));
			fp = new HashSet<>(); 
			System.out.println(" --- Leyendo los archivos anunciados ... --- " );
			fp = gson.fromJson(jr, SET_STOREFILE_TYPE);
			jr.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			JsonWriter wr = new JsonWriter(new BufferedWriter(new FileWriter(LOCAL_FILES_INFO)));
			fp = (Set<StoreFile>) ((fp == null)?new HashSet<>():fp);
			if (!containsFile(fp,f)) {
				System.out.println(" ---  Agregando archivo :  "+ f.getName() +", SHA-256 : "+f.getChecksum());
				fp.add(f);
				String str = gson.toJson(fp);
				wr.jsonValue(str);
			} else {
				System.out.println(" --- El archivo '"+ f.getName() +"["+f.getChecksum()+"] ya se encuentra declarado --- ");
			}
			wr.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	private boolean containsFile(Set<StoreFile> fp, StoreFile f) {
		for (StoreFile storeFile : fp) {
			if (storeFile.getChecksum().equals(f.getChecksum())) return true;
		}
		return false;
	}

	public void showFiles() throws IOException {
		JsonReader jr = new JsonReader(new InputStreamReader(new FileInputStream(LOCAL_FILES_INFO)));
		JsonParser jsonParser = new JsonParser();
		String userarray= jsonParser.parse(jr).getAsString();
		Set<StoreFile> fp = gson.fromJson(userarray, SET_STOREFILE_TYPE);
		System.out.println("\t[                     SHA-256                                  ]\t\t[      FILENAME      ]");
		for (StoreFile storeFile : fp) {
			System.out.println("\t"+storeFile.getChecksum() +"\t\t"+storeFile.getName());
		}
		jr.close();
	}

	public void deleteFile(StoreFile f) throws IOException {
		JsonReader jr;
		Set<StoreFile> fp = new HashSet<>();
		try {
			jr = new JsonReader(new FileReader(LOCAL_FILES_INFO));
			fp = new HashSet<>(); 
			System.out.println(" ---  Leyendo los archivos anunciados ... --- ");
			fp = gson.fromJson(jr, SET_STOREFILE_TYPE);
			System.out.println(gson.toJson(fp));
			jr.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			JsonWriter wr = new JsonWriter(new BufferedWriter(new FileWriter(LOCAL_FILES_INFO)));
			fp =(Set<StoreFile>) ((fp == null)?new HashSet<>():fp);
			System.out.println(gson.toJson(fp));
			if (containsFile(fp,f)) {
				System.out.println(" --- Agregando archivo :  "+ f.getName() +", SHA-256: "+f.getChecksum() +  " --- ");
				fp.remove(f);
				String str = gson.toJson(fp);
				wr.jsonValue(str);
			} else {
				System.out.println(" --- El archivo '"+ f.getName() +"'["+f.getChecksum()+"] no se encuentra declarado --- ");
			}
			wr.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	void modifyArchivos() {
		JsonReader jsr;
		try {
			jsr = new JsonReader(new FileReader(LOCAL_FILES_INFO));
			storeFiles = gson.fromJson(jsr, SET_STOREFILE_TYPE);
			jsr.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void announce() throws IOException, PException {
		if ((outputC == null) || (inputC == null)) connectToMain();
		Message m = new Message("announce");
		modifyArchivos();
		String fileChcks = extractFileChecksums(storeFiles);
		String fileNames = extractFileNames(storeFiles);
		if (fileChcks != "" && fileNames != "") {
			String[] arr1 = fileChcks.split(",");
			String[] arr2 = fileNames.split(",");
			System.out.println(" --- Anunciando los archivos al master  ---");
			System.out.println("\t[                     SHA-256                                  ]\t\t[      FILENAME      ]");
			for (int i = 0; i < arr2.length; i++) {
				System.out.println("\t"+arr1[i] + "\t\t" + arr2[i]);
			}
			m.setParametro("file-checksums", fileChcks);
			m.setParametro("file-names", fileNames);
			m.setParametro("port", String.valueOf(pPServer));
			m.setParametro("ip", String.valueOf(ipPServer));
			String json = gson.toJson(m);
			outputC.println(json);
			json = this.inputC.readLine();
			Message msg = gson.fromJson(json, Message.class);
		} else {
			System.out.println(" --- No hay archivos por anunciar al Master --- ");
		}
	}

	private Set<Seeder> findPeerWithFileByName(String name) throws IOException, PException{
		return findPeerWithFile(name, null);
	}
	
	private Set<Seeder> findPeerWithFileByChecksum(String chksum) throws IOException, PException{
		return findPeerWithFile(null, chksum);
	}

	private Set<Seeder> findPeerWithFile(String name, String chksum) throws IOException, PException{
		if ((outputC == null) || (inputC == null)) connectToMain();
		Message msge = new Message("find.peer.file");
		if (name != null) msge.setParametro("Name", name);
		if (chksum != null) msge.setParametro("Checksum", chksum);
		String json = gson.toJson(m);
		outputC.println(json);
		json = inputC.readLine();
		Message message = gson.fromJson(json, Message.class);
		String[] peerId;
		Set<Seeder> s = new HashSet<>();
		if (message.getParametro("peers") != null) { 
			peerId = message.getParametro("peers").split(",");
			for (String str : peerId) {
				String ip = str.split(":")[0];
				String port = str.split(":")[1];
				s.add(new Seeder(ip, port));
			}
		}
		return s;
	}

	private void findFilesAtPeer(String name) throws IOException, PException{
		if ((outputC == null) || (inputC == null)) connectToMain();
		Message msge = new Message("find.files.at.peer");
		if (name != null) msge.setParametro("Name", name);
		String json = gson.toJson(m);
		outputC.println(json);
		json = inputC.readLine();
		Message message = gson.fromJson(json, Message.class);
		String[] fileNames;
		String[] chcks;
		Set<Seeder> s = new HashSet<>();
		if (message.getParametro("file.names") != null) { 
			fileNames = message.getParametro("file.names").split(",");
			chcks = message.getParametro("file.checksums").split(",");
			System.out.println(" --- Los Archivos Disponibles para descargar son : --- ");
			System.out.println("\t[                     SHA-256                                  ]\t\t[      FILENAME      ]");
			for (int i = 0; i < fileNames.length; i++) {
				String chk = (chcks[i].length() > 8) ? chcks[i].substring(0, 8): chcks[i];
				System.out.println("\t"+ chcks[i] + "\t\t" + fileNames[i]);
			}
		}
	}


	public void downloadFile(String pathname, String chk, String destinationFolder, Seeder peer) throws ConnectException, NumberFormatException, UnknownHostException, IOException, PException {
		Socket s = new Socket(peer.getIp(), Integer.parseInt(peer.getPort()));
		BufferedReader inPeer = new BufferedReader (new InputStreamReader (s.getInputStream()));
		PrintWriter outputPeer = new PrintWriter (s.getOutputStream(),true);
		Message m = new Message("get.file");
		m.setParametro("Name", pathname);
		m.setParametro("Checksum", chk);
		String json = gson.toJson(m);
		outputPeer.println(json);
		json = inPeer.readLine();
		m = gson.fromJson(json, Message.class);
		if (m.getParametro("status") != null)
			System.out.println(" --- Estado del Seeder :" + m.getParametro("status") + " --- ");
			String name = m.getParametro("Name");
		if (s.isConnected()) {
			System.out.println(" --- Comenzando la descarga... --- ");
			try {
				InputStream in = s.getInputStream();
				OutputStream out = new FileOutputStream(destinationFolder + pathname);
				byte[] bytes = new byte[16*1024];
				int count;
				while ((count = in.read(bytes)) > 0) {
					out.write(bytes, 0, count);
					System.out.print("#");
				}
				in.close();
				out.close();
				System.out.println(" --- La descarga ha sido finalizada - El archivo fue guardado en: '" +destinationFolder+name+"' --- ");
			} catch (Exception e) {
				s.close();
				throw new PException(" --- ERROR en la descarga --- ");
			}
		}
		s.close();
	}

	public void menuClient() {
		System.out.println();
		System.out.println(" ---            MENÚ RED P2P            --- \n");
		System.out.println(" --- Acciones ha realizar sobre Red P2P --- \n");
		System.out.println("\t - find [filename|chksum]\t\tBusca los archivos por Nombre o por Checksum.");
		System.out.println("\t - download [filename] [dstFolder] Descarga los archivos por nombre. Además se especifica la carpeta destino.");
		System.out.println("\t - list-files [peerId|*]\t\tMuestra en formato de Lista todos los archivos que tiene un Seeder.");
		System.out.println("\n --- Acciones para administrar archivos a compartir sobre Red P2P --- \n");
		System.out.println("\t - set [path-file]\t\t\t Inicializa (Setea) el archivo Json, donde se contiene información sobre los archivos compartidos.");
		System.out.println("\t - add [path-file]\t\t\tAgrega el archivo a la lista de archivos compartidos.");
		System.out.println("\t - delete [path-file]\t\tBorra el archivo de la lista de archivos compartidos.");
		System.out.println("\t - show-files\t\t\tMuestra todos los archivos disponibles para compartir.");
		System.out.println("\n\t - options\t\t\t\tVuelve a mostrar el Menú.");
		System.out.println("\t - exit\t\t\t\tSalir.");
		System.out.println();
	}

	public String[] splitArgs(String command) {
		command = command.trim();
		return command.split(" ");
	}

	public void end() throws IOException {
	}

	public void interpretConsole(String line) throws PException, IOException, InterruptedException, NoSuchAlgorithmException  {
		String[] args = splitArgs(line);
		String command = args[0];
		if (command.equals("download")) {
			if (args.length < 3) throw new PException("  ---  No se especificaron los argumentos necesarios - Ingrese 'options' --- ");
			String destinationFolder = args[2];
			String denom = args[1];
			Set<Seeder> set;
			String name = null;
			String checksum = null;
			if (denom.length() > 60) {
				set = findPeerWithFileByChecksum(denom);
				checksum = denom;
			} else {
				set = findPeerWithFileByName(denom);
				name = denom;
			}
			Seeder[] p = set.toArray(new Seeder[set.size()]);
			boolean downloadOk = false;
			int i = 0;
			int intento = 0;
			if (p.length > 0) {
				System.out.println(" --- El Archivo : '"+denom+"' fue encontrado en "+p.length +" Peer/s --- ");
				while (!downloadOk) {
					try {
						if (intento++ > TRYS_IN_PEER) {
							i = (i==p.length)?0:i++;
							intento = 0;
						}
						downloadFile(name, checksum, destinationFolder, p[i]);
						downloadOk = true;
					} catch (PException e) {
						System.out.println(" --- ERROR con la descarga - Intentando con Peer:" + p[i].getPeerId() + "--- ");
						i++;
						if (i==p.length) break;
						Thread.sleep(RETRY_SLEEP_INTERVAL);
					} catch (ConnectException e) {
						System.out.println(" --- ERROR con la conexión - Peer ("+p[i].getPeerId()+") CAIDO --- ");
						i++;
						if (i==p.length) break;
						Thread.sleep(RETRY_SLEEP_INTERVAL);
					}  catch (Exception e) {
						e.printStackTrace();
					}
				}
			} else {
				System.out.println(" --- ERROR - No se encontró el archivo pedido ---");
			}
		} else if (command.equals("find")) {
			if (args.length < 2) throw new PException("  ---  No se especificaron los argumentos necesarios - Ingrese 'options' --- ");
			String denom = args[1];
			Set<Seeder> set;
			if (denom.length() > 60) set = findPeerWithFileByChecksum(denom);
			else set = findPeerWithFileByName(denom);
			Seeder[] p = set.toArray(new Seeder[set.size()]);
			if (p.length > 0) {
				System.out.println(" --- Los Seeders que tienen el archivo son : --- ");
				for (int i = 0; i < p.length; i++) {
					System.out.println("\t" + p[i].getPeerId());
				}
			} else {
				System.out.println(" --- ERROR - No se encontró el archivo pedido --- ");
			}
		} else if (command.equals("list-files")) {
			if (args.length < 2) throw new PException("   ---  No se especificaron los argumentos necesarios - Ingrese 'options' --- ");
			String peer = args[1];
			findFilesAtPeer(peer);
		} else if (command.equals("add")) {
			if (args.length < 2) throw new PException("  ---  No se especificaron los argumentos necesarios - Ingrese 'options' --- ");
			String pathname = args[1];
			try {
				addFile(new StoreFile(pathname));
				announce();
			} catch (IOException e) {
				System.out.println(" --- ERROR - El archivo pedido no existe --- ");
			} catch (PException e) {
				System.out.println(e);
			}
		}else if (command.equals("set")) {
			if (args.length < 2) throw new PException("   ---  No se especificaron los argumentos necesarios - Ingrese 'options' --- ");
			if (new FileReader(args[1]) != null) {
				LOCAL_FILES_INFO = args[1];
			}
		}else if (command.equals("delete")) {
			if (args.length < 2) throw new PException("   ---  No se especificaron los argumentos necesarios - Ingrese 'options' --- ");
			String pathname = args[1];
			try {
				deleteFile(new StoreFile(pathname));
				announce();
			} catch (IOException e) {
				System.out.println(" --- ERROR - El archivo pedido no existe --- ");
			} catch (PException e) {
				System.out.println(e);
			}
			
		} else if (command.equals("show-files")) {
			showFiles();
		} else if (command.equals("options")) {
			menuClient();
		} else if (command.equals("exit")) {
			throw new PException(" --- Ha salido de manera correcta --- ");
		} else {
			System.out.println(" --- ERROR - Opción Inválida --- ");
		}
	}

	void startClient() {
		scanner = new Scanner(System.in);
		System.out.println(" --- Ingrese - options - para ver las opciones --- ");
		while(true) {
			System.out.print("---> ");
			try {
				this.interpretConsole(scanner.nextLine());
			}  catch (PException e) {
				System.out.println(e.getMessage());
				if (e.getMessage().equals("  --- Ha salido de manera correcta ---")) break;
			} catch (Exception f) {
				System.out.println(f.getMessage());
			}
		}
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		startClient();
	}

}