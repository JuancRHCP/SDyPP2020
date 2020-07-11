package edu.unlu.sdypp.ej4.loadbalancer;

import ch.qos.logback.classic.Logger;
import com.jcraft.jsch.*;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import com.typesafe.config.ConfigObject;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class NodeMonitor implements Runnable {
    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(NodeMonitor.class);
    private Config config;
    private List<NodeReference> nodes;
    private List<NodeReference> availableNodes;
    private double criticalThreshold = 3;
    /**
     * Si la carga esta por encima de este valor, loggeo una alerta
     */
    private double maxThreshold = 2;
    /**
     * Si la carga esta por encima de este valor, agrego un nodo
     */
    private double minThreshold = 1;
    /**
     * Si la carga esta por debajo de este valor, elimino un nodo
     */
    private int maxNodes;
    /**
     * Cantidad de nodos maxima que esta permitido levantar
     */
    private int minNodes;
    /**
     * Cantidad de nodos minima que esta permitido levantar
     */
    private double load = 0;

    NodeMonitor(Config config) {
        this.config = config;
        this.nodes = Collections.synchronizedList(new ArrayList<NodeReference>());
        this.initNodeReferences(this.config);
    }

    public NodeReference getNodeReference() {
        int i = 0;
        NodeReference result = null;
        while (i < this.nodes.size() && result == null) {
            result = this.nodes.get(i);
            if (!result.isAvailable()) result = null;
            i++;
        }
        this.sortNodes();
        return result;
    }

    private void sortNodes() {
        synchronized (this.nodes) {
            Collections.sort(this.nodes);
        }
    }


    public void initNodeReferences(Config config) {
        int actualNodes = 0;
        try {
            this.minNodes = this.config.getInt("nodes.min");
            this.maxNodes = this.config.getInt("nodes.max");
            this.minThreshold = this.config.getInt("nodes.threshold.busy");
            this.maxThreshold = this.config.getInt("nodes.threshold.alert");
            criticalThreshold = this.config.getInt("nodes.threshold.critical");
            actualNodes = this.config.getObjectList("nodes.list").size();
        } catch (ConfigException e) {
            LOGGER.error("Config: error in nodes configuration");
        }
        if (actualNodes < this.minNodes) {
            this.minNodes = actualNodes;
        }
//        List<NodeReference> availableNodes = this.getConfiguredNodesList();
        this.availableNodes = this.getConfiguredNodesList();
        for (int i = 0; i < this.minNodes; i++) {
            try {
                NodeReference nr = availableNodes.get(i);
//                this.writeResourcesInNode(nr);
//                this.getCurrentDir(nr);
                boolean execResult = this.setUpNode(nr);
                if (execResult) {
                    LOGGER.info("Node started successfully: {}", nr);
                } else {
                    LOGGER.warn("Error trying to start node: {}", nr);
                }
            } catch (ConfigException e) {
                LOGGER.error("Config: error setting up new node: ", e);
            }
        }
        if (this.nodes.isEmpty()) {
            LOGGER.error("Config: no available nodes. Exiting...");
            System.exit(1);
        }
    }

    /**
     * @return Devuelve todos los nodos correctamente cargados
     */
    private List<NodeReference> getConfiguredNodesList() {
        List<NodeReference> result = this.availableNodes;
        if (result == null) {
            result = new ArrayList<NodeReference>();
            List<ConfigObject> actualNodes = (List<ConfigObject>) this.config.getObjectList("nodes.list");
            for (int i = 0; i < actualNodes.size(); i++) {
                try {
                    Config nodeConfig = actualNodes.get(i).toConfig();
                    NodeReference nodeReference = new NodeReference(nodeConfig);
                    result.add(nodeReference);
                } catch (ConfigException e) {
                    LOGGER.error("Config: error detected in node {}: ", i, e);
                }
            }
            this.availableNodes = result;
        }
        return result;
    }

    /**
     * Agrega un nuevo nodo a la lista (verifiando que no sea un que ya esta)
     */
    private boolean setUpNewNode() {
        List<NodeReference> unassignedNodes = this.availableNodes;
        unassignedNodes.removeAll(this.nodes);
        if (unassignedNodes.size() > 0) {
            NodeReference nr = unassignedNodes.get(0);
            String cmd = "java -jar ".concat(nr.getFilepath()).concat(" ").concat("Node ").concat(String.valueOf(nr.getPort()));
            boolean execResult = this.execInRemote(nr.getHost(), 22, nr.getPort(), nr.getUsername(), nr.getPassword(), cmd, null);
            if (execResult) {
                synchronized (this.nodes) {
                    this.nodes.add(nr);
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Solo para ser usado por el run().
     * Hago un random para que no devuelva siempre el mismo nodo. EJ: fallo en instanciar el nodo 3. Vuelve a pedir un nodo y le da otra vez el 3
     * y vuelve a dar error. Entonces, haciendo un random podria devolver el 4 u otro...
     */
    private NodeReference getAvailableNode() {
        List<NodeReference> unassignedNodes = this.availableNodes;
        unassignedNodes.removeAll(this.nodes);
        if (unassignedNodes.size() > 0) {
            Random random = new Random();
            NodeReference nr = unassignedNodes.get(random.nextInt(unassignedNodes.size()));
            return nr;
        }
        return null;
    }

    private boolean setUpNode(NodeReference nr) {
        String cmd = "java -jar ".concat(nr.getFilepath()).concat(" ").concat("Node ").concat(nr.getHost())
                .concat(" ").concat(String.valueOf(nr.getPort()));
        String resultado;
        boolean execResult = this.execInRemote(nr.getHost(), 22, nr.getPort(), nr.getUsername(), nr.getPassword(), cmd, null);
        if (execResult) {
            synchronized (this.nodes) {
                this.nodes.add(nr);
            }
            return true;
        }
        return false;
    }

    private String getCurrentDir(NodeReference nr) {
        String cmd = "dir";
        StringBuilder resultado = new StringBuilder();
        boolean execResult = this.execInRemote(nr.getHost(), 22, nr.getPort(), nr.getUsername(), nr.getPassword(), cmd, resultado);
        return resultado.toString();
    }

    private boolean removeNode(NodeReference nodeReference) {
        boolean result = false;
        nodeReference.setAvailable(false); // Para que no tome mas tareas
        while (nodeReference.getClients() > 0) {
            LOGGER.info("Waiting to remove node: {}", nodeReference);
        }
        String cmd = "uname";
        StringBuilder cmdResult = new StringBuilder(); // Uso StringBuilder ya que String no lo puedo pasar como param x referencia
        this.execInRemote(nodeReference.getHost(), 22, nodeReference.getPort(), nodeReference.getUsername(), nodeReference.getPassword(), cmd, cmdResult);
        LOGGER.debug("uname RESULTADO: "+ cmdResult.toString());
        if (cmdResult.toString().toLowerCase().contains("linux")) {
            cmd = "fuser -k ".concat(String.valueOf(nodeReference.getPort())).concat("/tcp");
        } else {
            cmd = "for /f \"skip=1 tokens=5\" %a in ('netstat -aon ^| find \":" + nodeReference.getPort() + "\" ^| find \"LISTENING\"') do taskkill /f /pid %a";
        }
        boolean execResult = this.execInRemote(nodeReference.getHost(), 22, nodeReference.getPort(), nodeReference.getUsername(), nodeReference.getPassword(), cmd, cmdResult);
        LOGGER.debug("kill RESULTADO: {}. execResult:{}", cmdResult.toString(), execResult);

        if (execResult) {
            synchronized (this.nodes) {
                result = this.nodes.remove(nodeReference);
                nodeReference.setAvailable(true); // Lo pongo disponible para la proxima vez que lo necesite
            }
        }
        return result;
    }


    @Override
    public void run() {
        while (true) {
            double totalClients = 0;
            for (NodeReference nodeReference : nodes) {
                totalClients += nodeReference.getClients();
            }
            this.load = ((totalClients / nodes.size()) + (totalClients % nodes.size()));

            if (this.load < this.minThreshold && this.nodes.size() > this.minNodes) {
                NodeReference nodeReference = this.nodes.get(0);
                LOGGER.info("System load is: {}. Removing node {}", this.load, nodeReference);
                if (this.removeNode(nodeReference))
                    LOGGER.info("Node removed: {}", nodeReference);
                else
                    LOGGER.info("Error removing node: {}", nodeReference);

            } else if (this.load > this.maxThreshold && this.nodes.size() < this.maxNodes) {
                LOGGER.info("System load is: {}. Setting up new node...", this.load);
                NodeReference nodeReference = this.getAvailableNode();
                if (nodeReference != null) {
                    if (this.setUpNode(nodeReference)) {
                        LOGGER.info("New node added {}. Current nodes: {}", nodeReference, this.nodes.size());
                    } else {
                        LOGGER.error("Error adding new node {}", nodeReference);
                    }
                } else {
                    LOGGER.error("Error adding new node. Probably no more available nodes");
                }
            }
            if (this.load > this.criticalThreshold && this.nodes.size() >= this.maxNodes) {
                LOGGER.warn("System load is: {}. CONSIDER ADDING NEW NODES", this.load);
            }

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                LOGGER.error("Error: ", e);
            }
        }
    }

    private boolean writeResourcesInNode(NodeReference node) {
        boolean result = false;
        String filePath = "";
        try {
            JSch jsch = new JSch();
            Session session = jsch.getSession(node.getUsername(), node.getHost(), 22);
            UserInfo userInfo = new MyUserInfo(node.getPassword());
            session.setUserInfo(userInfo);
            session.connect(120000);

            ChannelSftp channel = (ChannelSftp) session.openChannel("sftp");
            channel.connect(120000);

            filePath = channel.getHome() + File.separator + "application.conf";
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream("application.conf");
            LOGGER.info("Writing resource: {}", filePath);
            channel.put(inputStream, filePath);
//            filePath = channel.getHome() + File.separator + "run.sh";
//            inputStream = getClass().getClassLoader().getResourceAsStream("run.sh");
//            LOGGER.info("Writing resource: {}", filePath);
//            channel.put(inputStream, filePath);
//            filePath = channel.getHome() + File.separator + "stop.sh";
//            inputStream = getClass().getClassLoader().getResourceAsStream("stop.sh");
//            LOGGER.info("Writing resource: {}", filePath);
//            channel.put(inputStream, filePath);
            result = true;
        } catch (SftpException e) {
            LOGGER.error("Error writing resource({}):", filePath, e);
        } catch (JSchException e) {
            LOGGER.error("Error establishing ssh session to host {}:{}", node.getHost(), 22, e);
        }
        return result;
    }


    private String createScript(String cmd) {
        String currentPath = System.getProperty("user.home");
        Path path = Paths.get("");
        LOGGER.info("PATH: {}", currentPath);
        String filename = currentPath + File.separator + "nodeRunner.sh";
        LOGGER.info("Filename: {}", filename);
        // Powershell .ps1
        try {
            File file = new File(filename);
            PrintStream out = new PrintStream(new FileOutputStream(file));
            File fileToDelete = new File("pid.txt");
            if (fileToDelete.exists()) fileToDelete.delete();
//            cmd = "echo toma, gil.";
            out.println(cmd + " > pid.txt");
            out.close();
        } catch (Exception e) {

        }
        return filename;
    }

    public boolean execInRemote(String host, int sshPort, int nodePort, String user, String password, String cmd, StringBuilder output) {
        boolean result = true;
        String remoteId = String.format("[%s@%s:%d] ", user, host,sshPort);
        LOGGER.debug(remoteId + "CMD: {}", cmd);
        try {
            JSch jsch = new JSch();
            Session session = jsch.getSession(user, host, sshPort);
            // username and password will be given via UserInfo interface.
            UserInfo ui = new MyUserInfo(password);
            session.setUserInfo(ui);
            session.connect();
            Channel channel = session.openChannel("exec");
            ((ChannelExec) channel).setCommand(cmd);
            channel.setInputStream(null);
            InputStream in = channel.getInputStream();
            OutputStream out = channel.getOutputStream();
            InputStream err = ((ChannelExec) channel).getErrStream();
            BufferedWriter br = new BufferedWriter(new OutputStreamWriter(out));

            channel.connect();

            if (output == null) {output = new StringBuilder();}
            byte[] tmp = new byte[1024];
            try {
                Thread.sleep(1000);
            } catch (Exception ee) {
            }
            while (in.available() > 0) {
                int i = in.read(tmp, 0, 1024);
                if (i < 0) break;
                output.append(new String(tmp, 0, i));
                LOGGER.debug(remoteId + new String(tmp, 0, i));
            }
            while (err.available() > 0) {
                int i = err.read(tmp, 0, 1024);
                if (i < 0) break;
                output.append(new String(tmp, 0, i));
                LOGGER.debug(remoteId + new String(tmp, 0, i));
            }
            if (channel.isClosed()) {
                LOGGER.debug(remoteId + "Connection exit status: " + channel.getExitStatus());
                if (channel.getExitStatus() != 0 && channel.getExitStatus() != -1 && channel.getExitStatus() != 128)
                    result = false;
            }

            channel.disconnect();
            session.disconnect();
        } catch (Exception e) {
            LOGGER.error("Error trying to instantiate node in remote: ", e);
            result = false;
        }
        return result;
    }

    public class MyUserInfo implements UserInfo {
        String passwd;

        MyUserInfo(String passwd) {
            this.passwd = passwd;
        }

        public String getPassword() {
            return this.passwd;
        }

        public boolean promptYesNo(String str) {
            str = "Yes";
            return true;
        }

        public String getPassphrase() {
            return null;
        }

        public boolean promptPassphrase(String message) {
            return true;
        }

        public boolean promptPassword(String message) {
//            passwd="12345678"; // enter the password for the machine you want to connect.
            return true;
        }

        public void showMessage(String message) {

        }

    }
}
