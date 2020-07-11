package edu.unlu.sdypp.ej4.compute;

import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class Server {

    private static final int LISTEN_PORT = 8000;
    private static final String SERVICE_NAME = "ComputingServices";
    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(Server.class);

    private int clients;

    public Server(int port) {

    }

    /**
     * Este viene a ser el servidor: recibe las tareas y las ejecuta de acuerdo a su implementacion, en este caso, Pi.
     */
    public static void main(String[] args) {
        try {
            Registry rmiServer = LocateRegistry.createRegistry(LISTEN_PORT);
            Computable implementacion = new ImplementacionComputable();
            Computable servicio = (Computable) UnicastRemoteObject.exportObject(implementacion, LISTEN_PORT);
            rmiServer.rebind(SERVICE_NAME, servicio);
            LOGGER.info("Servicio \"{}\" iniciado en el puerto {}.", SERVICE_NAME, LISTEN_PORT);
        } catch (Exception e) {
            LOGGER.error("Server error:", e);
        }
    }
}
