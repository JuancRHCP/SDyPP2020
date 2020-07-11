package edu.unlu.sdypp.ej4.loadbalancer;

import edu.unlu.sdypp.ej4.client.DrawBorders;
import edu.unlu.sdypp.ej4.compute.Computable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Random;

public class TaskRunner implements Runnable{
    private static final Logger LOGGER = LoggerFactory.getLogger(TaskRunner.class);
    private NodeReference nodeReference;
    private DrawBorders drawBorders;
    private Object result;
    private int error;

    public <T> TaskRunner(NodeReference nodeReference, DrawBorders drawBorders) {
        this.nodeReference = nodeReference;
        this.drawBorders = drawBorders;
        this.error = 0;
    }

    @Override
    public void run() {
        try {
            LOGGER.info("TRATANDO DE CONECTARME AL NODO: {}", this.nodeReference);
            this.nodeReference.addClient();
            Registry registry = LocateRegistry.getRegistry(this.nodeReference.getHost(), this.nodeReference.getPort());
            LOGGER.info("REGISTRY LIST: {}", (Object[]) registry.list());
            Computable comp = (Computable) registry.lookup(this.nodeReference.getServiceName());
            LOGGER.info("COMPUTABLE: {}", comp);
            this.result = comp.ejecutarTarea(this.drawBorders);

            // Fuerzo un error para probar el manejo de errores.
            Random random = new Random();
            if (0 == random.nextInt(10)) {
                this.error = 3;
            }

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                this.error = 1;
                LOGGER.error("Error: ", e);
            }
            this.nodeReference.removeClient();

        } catch (RemoteException | NotBoundException e) {
            this.nodeReference.removeClient();
            this.error = 2;
            LOGGER.error("Error in RMI service: ", e);
//            throw new RuntimeException("LA CONCHA DE TU VIEJA");
        }

    }

    public NodeReference getNodeReference() {
        return nodeReference;
    }


    public Object getResultado() {
        return result;
    }

    public int getError() {
        return this.error;
    }
}
