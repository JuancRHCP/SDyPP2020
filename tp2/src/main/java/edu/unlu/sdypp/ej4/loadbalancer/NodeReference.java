package edu.unlu.sdypp.ej4.loadbalancer;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class NodeReference implements Comparable<NodeReference>{
    public static final Logger LOGGER = LoggerFactory.getLogger(NodeReference.class);

    private boolean isAvailable;
    private String serviceName;
    private String host;
    private int port;
    private Integer clients;
    private String username;
    private String password;
    private String filepath;

    public NodeReference(Config node) {
        if (node != null) {
            this.serviceName = node.getString("rmiServiceName");
            this.host = node.getString("host");
            this.port = node.getInt("port");
            this.username = node.getString("auth.username");
            this.password = node.getString("auth.password");
            this.filepath = node.getString("file");
            this.clients = 0;
            this.isAvailable = true;
        }
        LOGGER.info("Referenced node: {} {} {}", serviceName, host, port);
        if (node == null || serviceName == null || serviceName.isEmpty() || host == null || host.isEmpty() || port < 1 )
            throw new ConfigException.BugOrBroken("Invalid node configuration detected");
    }

    public NodeReference(String serviceName, String host, int port) {
        this.serviceName = serviceName;
        this.host = host;
        this.port = port;
        this.clients = 0;
        this.isAvailable = true;
    }

    public int addClient() {
        synchronized (this.clients) {
            if (this.isAvailable) {
                this.clients += 1;
                LOGGER.info("Nodo {} +1 Cliente. Total: {}", this.port, this.clients);
            }
        }
        return this.clients;
    }

    public int removeClient() {
        synchronized (this.clients) {
            this.clients -= 1;
            LOGGER.info("Nodo {} -1 Cliente. Total: {}", this.port, this.clients);
        }
        return this.clients;
    }

    public Integer getClients() {
        return clients;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFilepath() {
        return filepath;
    }

    public void setFilepath(String filepath) {
        this.filepath = filepath;
    }

    @Override
    public String toString() {
        return this.host + ":" + this.port + " (" + this.serviceName + ") - Clients: " + this.clients;
    }

    @Override
    /* Describe como es this respecto de o. */
    public int compareTo(NodeReference o) {
        return this.clients - o.getClients();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NodeReference that = (NodeReference) o;
        return port == that.port &&
                serviceName.equals(that.serviceName) &&
                host.equals(that.host) &&
                Objects.equals(username, that.username) &&
                Objects.equals(password, that.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serviceName, host, port, username, password);
    }
}
