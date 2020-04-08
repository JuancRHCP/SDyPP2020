package edu.unlu.sdypp.ej5;

import java.rmi.RemoteException;
import java.util.ArrayList;

public class Implementer implements RemoteI{

		
		public String getClima() throws RemoteException {
			
			ArrayList<String> lista_clima = new ArrayList<String>();
	        lista_clima.add("Soleado"); 
	        lista_clima.add("Nublado"); 
	        lista_clima.add("Lluvioso");
	        lista_clima.add("Tormentas eléctricas"); 
	        lista_clima.add("Ventoso"); 
			int i = (int)Math.random()*lista_clima.size();
			
			return lista_clima.get(i);
		}

	}
