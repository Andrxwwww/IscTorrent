package Messages;

import java.io.Serializable;
import java.net.InetAddress;

public class NewConnectionRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    private InetAddress clientAddress;
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
