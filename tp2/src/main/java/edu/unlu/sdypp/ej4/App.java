package edu.unlu.sdypp.ej4;

import edu.unlu.sdypp.ej4.client.Client;
import edu.unlu.sdypp.ej4.loadbalancer.LoadBalancer;
import edu.unlu.sdypp.ej4.loadbalancer.Node;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.rmi.RemoteException;

public class App {
    public static final Config CONFIG = ConfigFactory.load();

    public static void main( String[] args ) {
        if (args.length == 0) {
            System.out.println("Error: no args.");
            System.exit(0);
        }
        switch (args[0].toLowerCase()) {
            case "loadbalancer" : {
                System.out.println("LoadBalancer\n");
                System.setProperty("log.name","LoadBalancer");
                LoadBalancer loadBalancer = new LoadBalancer(Integer.valueOf(args[1]));
//                loadBalancer.initNodes();
                break;
            }
            case "node" : {
                System.out.println("Node\n");
                System.setProperty("log.name","Node");
                try {
                    Node node = new Node(args[1], Integer.valueOf(args[2]));
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            }
            case "client" : {
                System.out.println("Client\n");
                System.setProperty("log.name","Client");
                Client client = new Client(args[1], Integer.valueOf(args[2]));
                break;
            }
        }
    }
}
