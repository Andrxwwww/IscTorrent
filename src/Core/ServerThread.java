package Core;

import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

import Messages.NewConnectionRequest;

/**
 * Classe responsável por gerenciar as conexões de entrada no nó.
 * Ela escuta por novas conexões e cria um novo thread para lidar com cada conexão recebida.
 */

public class ServerThread extends Thread{

    private Node node;
    private ServerSocket serverSocket;

    /**
     * Construtor para a classe ServerThread.
     * @param node O nó associado a este thread.
     * @param port A porta na qual o servidor irá escutar por conexões.
     * @throws Exception Se ocorrer um erro ao criar o ServerSocket.
     */
    public ServerThread(Node node, int port) throws Exception {
        this.node = node;
        this.serverSocket = new ServerSocket(port);
    }

    /**
     * Método que inicia o servidor e aceita novas conexões.
     * Ele escuta por novas conexões e cria um novo thread para lidar com cada conexão recebida.
     */
    @Override
    public void run() {
        try {
            while (true){
                Socket socket = serverSocket.accept();
                System.out.println("Nova conexao recebida de: " + socket.getInetAddress().getHostAddress() + ":" + socket.getPort());

                ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
                Object obj = inputStream.readObject();

                // Cria um novo thread para lidar com a nova conexão
                if (obj instanceof NewConnectionRequest) {
                    NewConnectionRequest request = (NewConnectionRequest) obj;
                    System.out.println("Novo pedido de conexao recebido: " + request.toString());
                } else {
                    System.out.println("Mensagem recebida: " + obj.toString());
                }
            }
        } catch (Exception e) {
            System.out.println("Erro ao aceitar nova conexão: " + e.getMessage());
        } 

    }
    
}
