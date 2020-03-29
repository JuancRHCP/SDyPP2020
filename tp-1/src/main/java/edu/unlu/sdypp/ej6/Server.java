package edu.unlu.sdypp.ej6;

import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.logging.Level;

public class Server {

    public static final int LISTEN_PORT = 8000;
    public static final String SERVICE_NAME = "ArrayServices";
    private static Logger logger;

    public static void main( String[] args ) {
        logger = (Logger) LoggerFactory.getLogger(Server.class);

        try {
            // Creo el servidor. Abro un socket que escucha en ese puerto
            Registry serverRMI = LocateRegistry.createRegistry(LISTEN_PORT);

            // Creo la implementacion. El motor de procesamiento.
            Implementation implementation = new Implementation();

            // Exporto la implementaion como un servicio para que los clientes la consuman
            Services service = (Services) UnicastRemoteObject.exportObject(implementation, LISTEN_PORT);

            // Vinculo el servicio con un nombre amigable para el cliente que lo consume
            serverRMI.rebind(SERVICE_NAME, service);

//            logger.info(String.format("Servicio \"%s\" iniciado en el puerto %s.", SERVICE_NAME, LISTEN_PORT));
            logger.info("Servicio \"{}\" iniciado en el puerto {}.", SERVICE_NAME, LISTEN_PORT);

        } catch (RemoteException e) {
            logger.error("Server error: " + e);
        }
    }

}
