package Messages;

import Core.Node;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ConnectionHandler extends Thread {
    private Node node;
    private Socket socket;
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;

    public ConnectionHandler(Node node, Socket socket, ObjectInputStream input, ObjectOutputStream output) {
        this.node = node;
        this.socket = socket;
        this.inputStream = input;
        this.outputStream = output;
    }

    @Override
    public void run() {
        try {
            while (!socket.isClosed()) {
                Object obj = inputStream.readObject();
                if (obj instanceof NewConnectionRequest) {
                    System.out.println("NewConnectionRequest recebido novamente: " + obj);
                } else if (obj instanceof String) {
                    System.out.println("Mensagem recebida: " + obj);
                } else {
                    System.out.println("Mensagem desconhecida recebida: " + obj);
                }
            }
        } catch (Exception e) {
            System.out.println("Erro na conexão: " + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (Exception e) {
                System.out.println("Erro ao fechar socket: " + e.getMessage());
            }
        }
    }
}
