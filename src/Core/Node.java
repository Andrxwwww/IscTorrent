package Core;

import GUI.GUI;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class Node {

    private final int DEFAULT_PORT = 8080;
    
    private final int port;
    private final InetAddress address;
    private GUI gui;
    
    /**
     * Construtor para a classe GUI.
     * @param nodeID Identificador único para o noed.
     * @param gui gui associada ao node.
     */
    public Node(int nodeID , GUI gui){
        this.port = DEFAULT_PORT + nodeID;
        this.address = getLocalAddress();
        this.gui = gui;
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
        return port + " :" + address.getHostAddress();
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
