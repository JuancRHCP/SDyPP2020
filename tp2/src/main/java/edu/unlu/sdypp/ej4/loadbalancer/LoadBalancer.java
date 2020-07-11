package edu.unlu.sdypp.ej4.loadbalancer;

import edu.unlu.sdypp.ej4.App;
import edu.unlu.sdypp.ej4.client.DrawBorders;
import ch.qos.logback.classic.Logger;
import com.typesafe.config.ConfigException;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LoadBalancer {
    List<Node> nodos;
    List<NodeReference> nodes;

    @Deprecated
    public static final int LISTEN_PORT = 8000;
    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(LoadBalancer.class);
    private Integer clients;
    private NodeMonitor nodeMonitor;

    /**
     * Este viene a ser un servidor proxy: recibe las tareas y las ejecuta en uno de los nodos, balancenado asi la carga.
     */
    public static void main(String[] args) {
        LOGGER.info("Nodes: {}", App.CONFIG.getObjectList("nodes.list"));
        System.exit(0);

        try {
            ServerSocket ss = new ServerSocket(LISTEN_PORT);
            LOGGER.info("LoadBalancer iniciado en el puerto {}", LISTEN_PORT);
            LoadBalancer loadBalancer = new LoadBalancer(LISTEN_PORT);
            loadBalancer.initNodeReferences();

            while (true) {
                // aceptar clientes
                Socket cliente = ss.accept();
                LOGGER.info("Client conectado: " + cliente.getInetAddress().getCanonicalHostName() + " : " + cliente.getPort());
                ObjectOutputStream outputStream = new ObjectOutputStream(cliente.getOutputStream());
                ObjectInputStream inputStream = new ObjectInputStream(cliente.getInputStream());
                DrawBorders drawBorders = (DrawBorders) inputStream.readObject();
                LOGGER.info("Input: {}", drawBorders);
                int pieceQuantity = App.CONFIG.getInt("images.pieces");
                if (pieceQuantity < 1) {
                    LOGGER.warn("Config: number of pieces not detected. Using default: 1");
                    pieceQuantity = 1;
                }
                ClientHandler clientHandler = new ClientHandler(loadBalancer, drawBorders, pieceQuantity);
                Thread clientThread = new Thread(clientHandler);
                clientThread.start();
                LOGGER.info("clientThread.start() - {}", cliente.getPort());
                clientThread.join();
                LOGGER.info("clientThread.join() - {}", cliente.getPort());
                LOGGER.info("Result: {} - {}", clientHandler.getResult(), cliente.getPort());
                outputStream.writeObject(clientHandler.getResult());
                cliente.close();
            }
        } catch (IOException | InterruptedException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public LoadBalancer(int port) {
        this.clients = new Integer(0);
        this.nodes = Collections.synchronizedList( new ArrayList<NodeReference>() );
        this.nodos = new ArrayList<Node>();

//        NodeReference nodeReference = new NodeReference(App.CONFIG.getObject("nodes.node1").toConfig());
//        logger.info("Nodo configurado: {}", nodeReference);
//        this.nodes.add(nodeReference);
        this.initNodeReferences();

        try {
            ServerSocket ss = new ServerSocket(port);
            LOGGER.info("LoadBalancer iniciado en el puerto {}", port);

            while (true) {
                // aceptar clientes
                Socket cliente = ss.accept();
                LOGGER.info("Client conectado: " + cliente.getInetAddress().getCanonicalHostName() + " : " + cliente.getPort());
                ObjectOutputStream outputStream = new ObjectOutputStream(cliente.getOutputStream());
                ObjectInputStream inputStream = new ObjectInputStream(cliente.getInputStream());
                DrawBorders drawBorders = (DrawBorders) inputStream.readObject();
                LOGGER.info("Input: {}", drawBorders);
                int pieceQuantity;
                try {
                    pieceQuantity = App.CONFIG.getInt("images.pieces");
                } catch (ConfigException e) {
                    LOGGER.warn("Config: number of pieces not detected. Using default: 1");
                    pieceQuantity = 1;
                }
                ClientHandler clientHandler = new ClientHandler(this, drawBorders, pieceQuantity);
                Thread clientThread = new Thread(clientHandler);
                boolean error = true;
                while (error) {
                    try {
                        clientThread.start();
                        error = false;
                    } catch (OutOfMemoryError e) {
                        LOGGER.error("We cannot process your request now. Pleas wait a moment...");
                        error = true;
                    }
                }
                LOGGER.info("clientThread.start() - {}", cliente.getRemoteSocketAddress());
                clientThread.join();
                LOGGER.info("clientThread.join() - {}", cliente.getRemoteSocketAddress());
                outputStream.writeObject(clientHandler.getResult());
                cliente.close();
            }
        } catch (IOException | InterruptedException | ClassNotFoundException e) {
            LOGGER.error("Error: ", e);
        }
    }


    public void initNodeReferences() {
        /*
        int minThreshold = 0;
        int maxThreshold = 0;
        int criticalThreshold = 0;
        int min = 0;
        int max = 0;
        int actualNodes = 0;
        try {
            min = App.CONFIG.getInt("nodes.min");
            max = App.CONFIG.getInt("nodes.max");
            minThreshold = App.CONFIG.getInt("nodes.threshold.busy");
            maxThreshold = App.CONFIG.getInt("nodes.threshold.alert");
            criticalThreshold = App.CONFIG.getInt("nodes.threshold.critical");
            actualNodes = App.CONFIG.getObjectList("nodes.list").size();
        } catch (ConfigException e) {
            LOGGER.error("Config: error in nodes configuration");
        }
        if (actualNodes < min) {
            min = actualNodes;
        }
        for (int i = 0; i < min; i++) {
            try {
                Config nodeConfig = App.CONFIG.getObjectList("nodes.list").get(i).toConfig();
                NodeReference nodeReference = new NodeReference(nodeConfig);
                LOGGER.info("Node started: {}", nodeReference);
                this.nodes.add(nodeReference);
            } catch (ConfigException e) {
                LOGGER.error("Config: error setting up new node: ", e);
            }
        }
        if (this.nodes.isEmpty()) {
            LOGGER.error("Config: no availables nodes. Exiting...");
            System.exit(1);
        }*/

        this.nodeMonitor = new NodeMonitor(App.CONFIG);
        Thread nodeMonitorThread = new Thread(nodeMonitor);
        nodeMonitorThread.start();
    }


    /**
     * @return devuelve el primer nodo qe este disponible. Sino retorna nulo
     */
    NodeReference getNodeReference() {
        NodeReference result = this.nodeMonitor.getNodeReference();
        LOGGER.info("getNodeReference() --> {}", result);
        return result;
    }

}
