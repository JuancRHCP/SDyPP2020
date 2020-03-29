package edu.unlu.sdypp.ej6;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;

public class App {

	public static final int LISTEN_PORT = 8000;
	
	private static Logger logger = (Logger) LoggerFactory.getLogger(App.class);
	private static int[] vector1, vector2;
	private static Server server;
	private static Client client;
	
	public static void main(String[] args) {
		try {
		server = new Server(LISTEN_PORT);
		server.start();
		Thread.sleep(3000);
		client = new Client(LISTEN_PORT);
		
		vector1 = new int[] {1,2,3,4,5};
        vector2 = new int[] {2,2,2,2,2};
        logger.info("Vector1: {}", vector1);
        logger.info("Vector2: {}", vector2);
        Thread.sleep(3000);
        
        logger.info("sumar(vector1, vector2): {}", client.sumar(vector1,vector2));
        Thread.sleep(3000);
        
        logger.info("restar(vector1, vector2): {}", client.restar(vector1,vector2));
        Thread.sleep(3000);
        
        logger.info("Vectores luego de ser alterados en el servidor: \nvector1: {}. \nvector2: {}", vector1, vector2);
		
        Thread.sleep(3000);
        logger.info("Interrumpiendo servidor...");
        server.interruptService();
		} catch (InterruptedException e) {
			logger.warn("Interruption: ", e);
		} catch (RuntimeException e) {
			logger.error("Ha ocurrido un error. Interrumpiendo servidor...");
			server.interruptService();
		}
		logger.info("Programa finalizado");
	}

}
