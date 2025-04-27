package Messages;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class NewConnectionRequest implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    private java.net.InetAddress clientAddress;
    private int clientPort;

    /**
     * Construtor para a classe NewConnectionRequest.
     * @param clientAddress O endereço IP do cliente que está solicitando a conexão.
     * @param clientPort A porta do cliente que está solicitando a conexão.
     */
    public NewConnectionRequest(InetAddress clientAddress, int clientPort) {
        this.clientAddress = clientAddress;
        this.clientPort = clientPort;
    }

    public InetAddress getClientAddress() {
        return clientAddress;
    }
    
    public int getClientPort() {
        return clientPort;
    }

    @Override
    public String toString() {
        return "NewConnectionRequest [clientAddress=" + clientAddress.getHostAddress() + " | clientPort=" + clientPort + "]";
    }   
    
}
