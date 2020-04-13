package edu.unlu.sdypp.ej7.client;

import ch.qos.logback.classic.Logger;
import edu.unlu.sdypp.ej7.compute.Computable;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Client {

    private static final int LISTEN_PORT = 8000;
    private static final String SERVICE_NAME = "ComputingServices";
    private static final Logger logger = (Logger) LoggerFactory.getLogger(Client.class);

    /**
     * Es la clase cliente. Creo un objeto Pi, que es una tarea y se la entrega al servidor para que haga el calculo.
     */
    public static void main(String[] args) {
        try {
            Registry registry = LocateRegistry.getRegistry(LISTEN_PORT);
            Computable comp = (Computable) registry.lookup(SERVICE_NAME);
            int decimalDigits = 4;
            Pi task = new Pi(decimalDigits);
            BigDecimal piValue = comp.ejecutarTarea(task);
            logger.info("El valor de Pi (con {} decimales) es: {}", decimalDigits, piValue);
        } catch (RemoteException | NotBoundException e) {
            logger.error("Client error: ", e);
        }
    }
}
