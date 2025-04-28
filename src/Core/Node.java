package Core;

import GUI.GUI;
import Messages.NewConnectionRequest;
import Messages.Connection;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.*;

import Download.FileBlockRequestMessage;

public class Node {

    private final int DEFAULT_PORT = 8080;
    
    private final int port;
    private final InetAddress address;
    private GUI gui;
    private final String workFolder;
    private List<FileSearchResult> localFiles = new ArrayList<FileSearchResult>();
    private ServerThread server;
    private Map<String, Connection> activeConnections = new HashMap<String, Connection>();


    
    /**
     * Construtor para a classe GUI.
     * @param nodeID Identificador único para o noed.
     * @param gui gui associada ao node.
     */
    public Node(int nodeID , GUI gui){
        this.port = DEFAULT_PORT + nodeID;
        this.address = getLocalAddress();
        this.gui = gui;
        this.workFolder = "files/dl"+nodeID;
        loadLocalFiles();
        startServing();
    }

    //GETTERS

    /**
     * * Retorna o endereço IP local do nó.
     * @return Endereço IP local do nó.
     * @throws UnknownHostException se ocorrer um erro ao obter o endereço IP local.
     * foi feito à parte de modo haver debugging com a mensagem de erro
     */
    public InetAddress getLocalAddress() {
        try {
            return InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            throw new RuntimeException("1.1 -> Erro ao obter o endereço IP local", e);
        }
    }

    /**
     * Retorna o endereço IP do nó.
     * @return Endereço IP do nó.
     */
    public String getPortAndAdress() {
        return address.getHostAddress() + ": " + port;
    }

    /**
     * Retorna a lista de arquivos locais.
     * @return Lista de arquivos locais.
     */
    public List<FileSearchResult> getLocalFiles() {
        return localFiles;
    }

    //METODOS

    /**
     * Phase 2
     * Carrega os arquivos locais do nó.
     * Cria a pasta de trabalho se não existir.
     * Lê os arquivos da pasta e calcula o hash de cada um.
     */
    private void loadLocalFiles() {
        try {

            File folder = new File(workFolder);

            if (!folder.exists() || !folder.isDirectory()) {
                boolean created = folder.mkdirs();
                System.out.println("Diretoria criada: " + workFolder + " (" + created + ")");
            }
    
            File[] files = folder.listFiles();
    
            System.out.println("\nNode [" + getPortAndAdress() + "] - ficheiros carregados:");
    
            if (files != null && files.length > 0) {
                for (File file : files) {

                    String fileName = file.getName();
                    long fileSize = file.length();
                    String hash = calculateFileHash(file);
    
                    FileSearchResult result = new FileSearchResult( fileName, hash, fileSize, address, port);
    
                    localFiles.add(result);
                    System.out.println( result.toStringFull() );
                }
            } else {
                System.out.println("1.2 -> Nenhum ficheiro encontrado nesta diretoria.");
            }
    
            System.out.println("-------------------------------------------\n");
        } catch (Exception e) {
            System.err.println("Erro durante loadLocalFiles()" + e.getMessage());
        }
    }

    /**
     * Phase 2
     * Calcula o hash SHA-256 de um arquivo.
     * @param file O arquivo para calcular o hash.
     * @return O hash do arquivo em formato hexadecimal.
     */
    private String calculateFileHash(File file){
        try {
            
            byte[] fileBytes = Files.readAllBytes(file.toPath());
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(fileBytes);

            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();

        } catch (Exception e) {
            System.err.println("Erro durante calculateFileHash()" + e.getMessage());
            return null;
        }
    }
    
    /**
     * Phase 4
     * Inicia o servidor para aceitar conexões de outros nós.
     */
    private void startServing(){
        try {
            server = new ServerThread(this, port);
            server.start();
        } catch (Exception e) {
            System.err.println("Erro ao iniciar o servidor: " + e.getMessage());
        }
    }

    /**
     * Phase 4
     * Passa de String para InetAddress.
     * @param address O endereço IP do nó ao qual se conectar.
     */
    public InetAddress stringToInetAddress(String address) {
        try {
            return InetAddress.getByName(address);
        } catch (Exception e) {
            System.err.println("Erro ao converter String para InetAddress: " + e.getMessage());
            return null;
        }
    }




    /**
     * Phase 4
     * Conecta-se a outro nó usando o endereço e a porta fornecidos.
     * @param address O endereço IP do nó ao qual se conectar.
     * @param port A porta do nó ao qual se conectar.
     */
    public void connectToNode(String address, int port) {

        InetAddress targetAddress = stringToInetAddress(address);

        // Verifica se a conexão é válida antes de tentar se conectar
        if (!isValidConnection(targetAddress, this.port ,port)) {
            return;
        }

        try {
            Socket socket = new Socket(address, port);
            System.out.println("Conectado com SUCESSO ao nó em " + targetAddress.getHostAddress() + ":" + port);

            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());

            NewConnectionRequest request = new NewConnectionRequest(this.address, this.port);
            //System.out.println("Socket local port: " + socket.getLocalPort() + " | Socket port: " + socket.getPort());
            outputStream.writeObject(request);
            outputStream.flush();

            addConnection(targetAddress, port, socket, inputStream, outputStream);

        } catch (Exception e) {
            System.err.println("Erro ao conectar ao nó: " + e.getMessage());
        }
    }


    public void addConnection(InetAddress address, int port, Socket socket, ObjectInputStream input, ObjectOutputStream output) {
        String key = address.getHostAddress() + ":" + port;
        activeConnections.put(key, new Connection(socket, input, output));
        System.out.println("Conexão adicionada: " + key);
    }

    /**
     * Phase 4
     * Verifica se já existe uma conexão com o nó especificado.
     * @param address O endereço IP do nó ao qual se conectar.
     * @param port A porta do nó ao qual se conectar.
     */
    public boolean isAlreadyConnected(InetAddress targetAddress, int targetPort) {
        String key = targetAddress.getHostAddress() + ":" + targetPort;
        return activeConnections.containsKey(key);
    }


    /**
     * Phase 4
     * Verifica se a conexão é válida antes de tentar se conectar a outro nó.
     * @param address O endereço IP do nó ao qual se conectar.
     * @param port A porta do nó ao qual se conectar.
     */
    private boolean isValidConnection(InetAddress targetAddress, int originPort ,int targetPort) {
        if (targetPort < 1024 || targetPort > 65535) {
            System.out.println(getPortAndAdress() + " -> Falha: intervalo de portas inválido");
            return false;
        }
    
        if (targetAddress.equals(this.address) && targetPort == originPort) {
            System.out.println(getPortAndAdress() + " -> Falha: tentativa de conectar a si próprio");
            return false;
        }
    
        if (isAlreadyConnected(targetAddress, targetPort)) {
            System.out.println(getPortAndAdress() + " -> Falha: já existe ligação a este nó");
            return false;
        }
    
        return true;
    }


    public void broadcastSearch(String searchText) {
        // Implementar a lógica de broadcast de pesquisa aqui
        System.out.println("Broadcasting search for: " + searchText);
    }

}
