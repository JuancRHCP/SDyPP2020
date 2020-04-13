package edu.unlu.sdypp.ej5;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;

public class Server{

	public static void main(String[] args) {
		
		
		try {
			Implementer s= new Implementer();
			System.out.println(" -- El implementador fue instanciado -- ");
			
			Registry server = LocateRegistry.createRegistry(90);
			System.out.println(" --- El Servicio RMI fue iniciado --- ");
			
			RemoteI ServerClima = (RemoteI) UnicastRemoteObject.exportObject(s, 9000);
			System.out.println(" --- El servicio del clima fue asociado a un puerto --- ");
			
			server.rebind("Info_del_clima", ServerClima);
			System.out.println(" ---El bind de servicio JNDI fue realizado --- ");
	
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}