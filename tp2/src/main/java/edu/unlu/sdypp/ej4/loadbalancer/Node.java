package edu.unlu.sdypp.ej4.loadbalancer;

import edu.unlu.sdypp.ej4.compute.Computable;
import edu.unlu.sdypp.ej4.compute.ImplementacionComputable;
import edu.unlu.sdypp.ej4.compute.Tarea;
import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;


public class Node implements Computable {
    private final static Logger LOGGER = (Logger) LoggerFactory.getLogger(Node.class);
    private static final String SERVICE_NAME = "ComputingServices";;
    private Computable servicio;
    private Integer clients;
    private String host;
    private int port;


    public static void main(String[] args) throws RemoteException {
        Integer clients = new Integer(0);
        int port = 8000;
        Registry rmiServer = LocateRegistry.createRegistry(port); // Random Port
        Computable implementacion = new ImplementacionComputable();
        Computable servicio = (Computable) UnicastRemoteObject.exportObject(implementacion,port);
        rmiServer.rebind(SERVICE_NAME, servicio);
        LOGGER.info("Servicio \"{}\" iniciado en el puerto {}. => {}", SERVICE_NAME, port, servicio);
    }

    public Node(String host, int port) throws RemoteException {
        this.clients = 0;
        this.host = host;
        this.port = port;
//        if (System.getSecurityManager() == null) {
//            System.setSecurityManager(new SecurityManager());
//        }
        // Para que no ponga hostname 127.0.0.1 en los nodos remotos
//        System.setProperty("java.rmi.server.hostname", this.host);
//        LOGGER.info("SET PROPERTY: java.rmi.server.hostname={}", System.getProperty("java.rmi.server.hostname"));
        Registry rmiServer = LocateRegistry.createRegistry(this.port); // Random Port
        Computable implementacion = new ImplementacionComputable();
        this.servicio = (Computable) UnicastRemoteObject.exportObject(implementacion, this.port);
        rmiServer.rebind(SERVICE_NAME, servicio);
        LOGGER.info("Servicio \"{}\" iniciado en el puerto {}. => {}", SERVICE_NAME, this.port, servicio);
    }

    @Override
    public <T> T ejecutarTarea(Tarea<T> t) throws RemoteException {
        synchronized (this.clients) {
            this.clients += 1;
            LOGGER.info("Nodo {} +1 Cliente. Total: {}", this.port, this.clients);
        }

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 2. Ejecutar tarea
        T result = this.servicio.ejecutarTarea(t);
        synchronized (this.clients) {
            this.clients -= 1;
            LOGGER.info("Nodo {} -1 Cliente. Total: {}", this.port, this.clients);
        }
        return result;
    }

    public int getPort() {
        return port;
    }
}
