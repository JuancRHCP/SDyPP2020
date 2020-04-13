package edu.unlu.sdypp.ej5;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import edu.unlu.sdypp.ej1.Client;

public class ClientR {
		
		public static void main(String[] args) {
			// TODO Auto-generated method stub
			try {
				
				Registry rmi = LocateRegistry.getRegistry("localhost", 90);
			    System.out.println(" --- El Cliente está conectado --- ");
				
				RemoteI r = (RemoteI) rmi.lookup("Info_del_clima");
				String Info_del_clima = r.getClimaServer();
				System.out.println(" --- El Clima en la región del servidor es: " + Info_del_clima);
				System.out.println(" ------------- F I N A L I Z A D O --------------- ");
				
				
			} catch (RemoteException e) {
				e.printStackTrace();
			} catch (NotBoundException e) {
				e.printStackTrace();
			}
		}

	}

