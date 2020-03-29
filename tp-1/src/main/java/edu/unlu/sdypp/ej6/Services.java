package edu.unlu.sdypp.ej6;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Services extends Remote {
    int[] sumar (int[] v1, int[] v2) throws RemoteException;
    int[] restar (int[] v1, int[] v2) throws RemoteException;
}
