package edu.unlu.sdypp.ej4.loadbalancer;

import edu.unlu.sdypp.ej4.client.DrawBorders;
import edu.unlu.sdypp.ej4.util.ImageUtils;
import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ClientHandler implements Runnable {
    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(ClientHandler.class);
    private LoadBalancer loadBalancer;
    private DrawBorders drawBorders;
    private ImageIcon result;
    private List<DrawBorders> pieces;
    private List<Thread> threads;
    private List<TaskRunner> taskRunners;
    private int pieceQuantity;

    public ClientHandler(LoadBalancer loadBalancer, DrawBorders drawBorders, int pieceQuantity) {
        this.loadBalancer = loadBalancer;
        this.drawBorders = drawBorders;
        this.pieceQuantity = pieceQuantity;
    }

    @Override
    public void run() {
        try {
            this.pieces = new ArrayList<DrawBorders>();
            this.threads = new ArrayList<Thread>();
            this.taskRunners = new ArrayList<TaskRunner>();
            // Lanzo un Thread para cada pedazo
            for (ImageIcon imageIcon : ImageUtils.cropIntoColumns(drawBorders.getImage(), this.pieceQuantity)) {
                pieces.add(new DrawBorders(imageIcon));
            }

            for (int i = 0; i < pieces.size(); i++) {
                this.runTaskInNewThread(i, false);
            }

            // Barrera de sincronizacion
            while (!threads.isEmpty()) {
                int i = 0;
                while (i < threads.size()) {
                    if (taskRunners.get(i).getError() == 0) {
                        if (threads.get(i).getState().equals(Thread.State.TERMINATED)) {
                            threads.remove(threads.get(i));
                        }
                    } else {
                        LOGGER.warn("Error running task {}", i);
                        this.runTaskInNewThread(i, true);
                    }
                }
            }
        } catch (RuntimeException e) {
            LOGGER.error("LA CONCHA DE TU MADRE ALL BOYS: ", e);
        }

        // Terminaron de procesarse todos los pedazos. Los uno
        List<Image> processedPieces = new ArrayList<Image>();
        for (TaskRunner taskRunner : taskRunners) {
            Image bi = ImageUtils.imageIconToBufferedImage((ImageIcon) taskRunner.getResultado());
            processedPieces.add(bi);
        }
        this.result = new ImageIcon(ImageUtils.mergeColumnsIntoImage(processedPieces));
    }

    private void runTaskInNewThread(int i, boolean reRun) {
        // Para que un mismo nodo no tome todas las partes de la imagen, es que lo pido aca.
        NodeReference nodeReference = null;
        while (nodeReference == null) {
            nodeReference = this.loadBalancer.getNodeReference();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                LOGGER.error("Fatal error: ", e);
                throw new RuntimeException(e);
            }
        }
        TaskRunner taskRunner = new TaskRunner(nodeReference, pieces.get(i));
        // De nombre le pongo el numero de piece. Para saber cual es si llega a fallar
        Thread thread = new Thread(taskRunner, String.valueOf(i));
        if (reRun) {
            taskRunners.remove(i);
            taskRunners.add(i, taskRunner);
            threads.remove(i);
            threads.add(i, thread);
            LOGGER.warn("Re-starting task {} in node: {}:{}", i, nodeReference.getHost(), nodeReference.getPort());
        } else {
            taskRunners.add(taskRunner);
            threads.add(thread);
            LOGGER.info("Starting task {} in node: {}:{}", i, nodeReference.getHost(), nodeReference.getPort());
        }
        Thread.UncaughtExceptionHandler h = new Thread.UncaughtExceptionHandler() {
            public void uncaughtException(Thread th, Throwable ex) {
                System.out.println("CATCH MILAGROSO?: " + ex);
            }
        };
        thread.setUncaughtExceptionHandler(h);
        thread.start();
    }

    public ImageIcon getResult() {
        return result;
    }
}