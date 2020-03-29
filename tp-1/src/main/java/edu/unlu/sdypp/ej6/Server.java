package edu.unlu.sdypp.ej6;

import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class Server extends Thread{

    public static final String SERVICE_NAME = "ArrayServices";
    private static Logger logger = (Logger) LoggerFactory.getLogger(Server.class);
    
    private Registry rmiServer;
    private Implementation implementation;
    private Services service;
    private int port;
    private boolean interruptFlag = false;
    
    public Server(int port) {
    	this.port = port;
    }

	public void run() {
		try {
			
			// Creo el servidor. Abro un socket que escucha en ese puerto
            this.rmiServer = LocateRegistry.createRegistry(this.port);
            
            // Creo la implementacion. El motor de procesamiento.
            this.implementation = new Implementation();

            // Exporto la implementaion como un servicio para que los clientes la consuman
            this.service = (Services) UnicastRemoteObject.exportObject(implementation, port);

            // Vinculo el servicio con un nombre amigable para el cliente que lo consume
            this.rmiServer.rebind(SERVICE_NAME, service);

            logger.info("Servicio \"{}\" iniciado en el puerto {}.", SERVICE_NAME, port);
            
            while (!this.interruptFlag) {
            	Thread.sleep(1000);
            }
            System.exit(0);
            
        } catch (RemoteException e) {
            logger.error("Server error: " + e);
        } catch (InterruptedException e) {
        	logger.warn("Interruption: ", e);
        }
	}
	
	public void interruptService() {
		this.interruptFlag = true;
	}

}
