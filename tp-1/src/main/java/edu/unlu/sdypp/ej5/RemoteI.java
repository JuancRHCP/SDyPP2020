package edu.unlu.sdypp.ej5;

import java.rmi.Remote;
import java.rmi.RemoteException;

	public interface RemoteI extends Remote{
		public String getClimaS() throws RemoteException;
		
}
