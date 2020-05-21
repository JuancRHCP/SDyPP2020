package edu.unlu.sdypp.ej1.Peer;

import java.util.Scanner;

public class PeerMain {
	
	public static void main(String[] args) {
		int t = (int) Thread.currentThread().getId();
		String NameComp = SServer.class.getSimpleName().toString()+"-"+ t ;
		System.setProperty("log.name",NameComp);
		Scanner sc = new Scanner(System.in);
		System.out.println(" --- Ingrese el puerto del SeederServer: ---  ");
		int pPort;
		do {
			pPort = sc.nextInt();
			if (pPort < 1025 || pPort > 65536) System.out.println(" --- ERROR - Ingrese un puerto MAYOR a 1024 o MENOR a 65536");
		} while(pPort < 1025 || pPort > 65535);
		SServer ss = new SServer(pPort);
		Thread tServer = new Thread(ss);
		PeerClient pc = new PeerClient(pPort, "localhost");
		Thread tClient = new Thread(pc);
		tServer.start();
		tClient.start();
		try {
			tClient.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}