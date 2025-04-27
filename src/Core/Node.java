package Core;

import GUI.GUI;
import Messages.NewConnectionRequest;

import java.io.File;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
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
    private List<Socket> connections = new ArrayList<>();

    
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
        if (!isValidConnection(targetAddress, port)) {
            return;
        }

        try {
            Socket socket = new Socket(address, port);
            System.out.println("Conectado com SUCESSO ao nó em " + targetAddress.getHostAddress() + ":" + port);

            // Enviar uma solicitação de conexão ou outra mensagem, se necessário
            NewConnectionRequest request = new NewConnectionRequest(this.address, this.port);
            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
            outputStream.writeObject(request);
            outputStream.flush();

            connections.add(socket);

        } catch (Exception e) {
            System.err.println("Erro ao conectar ao nó: " + e.getMessage());
        }
    }

    /**
     * Phase 4
     * Verifica se já existe uma conexão com o nó especificado.
     * @param address O endereço IP do nó ao qual se conectar.
     * @param port A porta do nó ao qual se conectar.
     */
    private boolean isAlreadyConnected(InetAddress targetAddress, int targetPort) {
        for (Socket socket : connections) {
            if (socket.getInetAddress().equals(targetAddress) && socket.getPort() == targetPort) {
                return true;
            }
        }
        return false;
    }


    /**
     * Phase 4
     * Verifica se a conexão é válida antes de tentar se conectar a outro nó.
     * @param address O endereço IP do nó ao qual se conectar.
     * @param port A porta do nó ao qual se conectar.
     */
    private boolean isValidConnection(InetAddress targetAddress, int targetPort) {
        if (targetPort < 1024 || targetPort > 65535) {
            System.out.println(getPortAndAdress() + " -> Falha: intervalo de portas inválido");
            return false;
        }
    
        if (targetAddress.equals(this.address) && targetPort == this.port) {
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
