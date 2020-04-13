package edu.unlu.sdypp.ej5;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Random;

public class Implementer implements RemoteI{

		
		public String getClimaServer() throws RemoteException {
			
			ArrayList<String> lista_de_climas = new ArrayList<String>();
	        lista_de_climas.add("Soleado"); 
	        lista_de_climas.add("Nublado"); 
	        lista_de_climas.add("Lluvioso");
	        lista_de_climas.add("Tormentas eléctricas");
	        lista_de_climas.add("Ventoso"); 

			Random random = new Random();
			int i = random.nextInt(lista_de_climas.size());

			return lista_de_climas.get(i);
			}

	}
