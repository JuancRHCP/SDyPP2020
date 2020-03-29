package edu.unlu.sdypp.ej6;

import ch.qos.logback.classic.Logger;
import com.sun.istack.internal.Pool;
import org.slf4j.LoggerFactory;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Client {

    private static Logger logger;
    private static int[] vector1, vector2;

    public static void main(String[] args) {
        logger = (Logger) LoggerFactory.getLogger(Client.class);

        vector1 = new int[]{1,2,3,4,5};
        vector2 = new int[]{2,2,2,2,2};

        try {
            Registry clientRMI = LocateRegistry.getRegistry("127.0.0.1", Server.LISTEN_PORT);

            String[] services = clientRMI.list();
            Services service = (Services) clientRMI.lookup(services[0]);
            logger.info("Servicios disponibles: {} \nServicio elegido: {}", services, services[0]);

            logger.info("Vector1: {}", vector1);
            logger.info("Vector2: {}", vector2);
            logger.info("sumar(vector1, vector2): {}", service.sumar(vector1,vector2));
            logger.info("restar(vector1, vector2): {}", service.restar(vector1,vector2));
            logger.info("Vectores luego de ser alterados en el servidor: \nvector1: {}. \nvector2: {}", vector1, vector2);

        } catch (RemoteException e) {
            logger.error(e.getMessage());
            logger.warn(e.toString());
        } catch (NotBoundException e) {
            logger.error(e.getMessage());
        }
    }

}
