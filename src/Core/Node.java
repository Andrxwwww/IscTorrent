package Core;

import GUI.GUI;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

public class Node {

    private final int DEFAULT_PORT = 8080;
    
    private final int port;
    private final InetAddress address;
    private GUI gui;
    private final String workFolder;
    private List<FileSearchResult> localFiles = new ArrayList<FileSearchResult>();
    
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
                    int hash = Objects.hash(fileName, fileSize); // calcular o hash
    
                    FileSearchResult result = new FileSearchResult( fileName, hash, fileSize, address, port);
    
                    localFiles.add(result);
                    System.out.println(result);
                }
            } else {
                System.out.println("1.2 -> Nenhum ficheiro encontrado nesta diretoria.");
            }
    
            System.out.println("-------------------------------------------\n");
        } catch (Exception e) {
            System.err.println("Erro durante loadLocalFiles()" + e.getMessage());
        }
    }
    

    public void connectToNode(String address, int port) {
        // Implementar a lógica de conexão a outro nó aqui
        System.out.println("Conectando ao nó em " + address + ":" + port);
    }

    public void broadcastSearch(String searchText) {
        // Implementar a lógica de broadcast de pesquisa aqui
        System.out.println("Broadcasting search for: " + searchText);
    }

}
