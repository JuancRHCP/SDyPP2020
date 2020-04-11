package edu.unlu.sdypp.ej7.compute;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Guia: https://docs.oracle.com/javase/tutorial/rmi/designing.html
 * es un objeto "computable": una interfaz para generalizar objetos y/o servicios que son ejecutables por el servidor.
 */
public interface Computable extends Remote {
    <T> T ejecutarTarea(Tarea<T> t) throws RemoteException;
}
