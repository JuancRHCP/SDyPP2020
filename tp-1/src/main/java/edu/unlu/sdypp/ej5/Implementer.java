package edu.unlu.sdypp.ej5;

import java.rmi.RemoteException;
import java.util.ArrayList;

public class Implementer implements RemoteI{

		
		public String getClimaServ() throws RemoteException {
			
			ArrayList<String> lista_climas = new ArrayList<String>();
	        lista_climas.add("Soleado"); 
	        lista_climas.add("Nublado"); 
	        lista_climas.add("Lluvioso");
	        lista_climas.add("Tormentas eléctricas "); 
	        lista_climas.add("Ventoso"); 
			int i = (int)Math.random()*lista_climas.size();
			
			return lista_climas.get(i);
			}

	}
