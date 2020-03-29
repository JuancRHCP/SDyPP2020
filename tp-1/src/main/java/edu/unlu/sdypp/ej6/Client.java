package edu.unlu.sdypp.ej6;

import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Client {

    private static Logger logger = (Logger) LoggerFactory.getLogger(Client.class);
    private static int[] vector1, vector2;
    private Registry rmiClient;
    private Services service;

    public Client(int port) {

        vector1 = new int[]{1,2,3,4,5};
        vector2 = new int[]{2,2,2,2,2};

        try {
        	this.rmiClient = LocateRegistry.getRegistry("127.0.0.1", port);

            String[] services = this.rmiClient.list();
            this.service = (Services) this.rmiClient.lookup(services[0]);
            logger.info("Servicios disponibles: {} \nServicio elegido: {}", services, services[0]);
/*
            logger.info("Vector1: {}", vector1);
            logger.info("Vector2: {}", vector2);
            logger.info("sumar(vector1, vector2): {}", service.sumar(vector1,vector2));
            logger.info("restar(vector1, vector2): {}", service.restar(vector1,vector2));
            logger.info("Vectores luego de ser alterados en el servidor: \nvector1: {}. \nvector2: {}", vector1, vector2);
*/
        } catch (RemoteException | NotBoundException e) {
            logger.error(e.getMessage());
        }
    }
    
    public int[] sumar(int[] v1, int[] v2) {
    	if (service == null) {
    		logger.error("service is null.");
    		throw new RuntimeException("service is null.");
    	} else {
    		try {
				return service.sumar(v1, v2);
			} catch (RemoteException e) {
				logger.error(e.getMessage());
				throw new RuntimeException(e.getMessage());
			}
    	}
    }
    
    public int[] restar(int[] v1, int[] v2) {
    	if (service == null) {
    		logger.error("service is null.");
    		throw new RuntimeException("service is null.");
    	} else {
    		try {
				return service.restar(v1, v2);
			} catch (RemoteException e) {
				logger.error(e.getMessage());
				throw new RuntimeException(e.getMessage());
			}
    	}
    }
}
