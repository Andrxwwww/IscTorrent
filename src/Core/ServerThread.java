package Core;

import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import Messages.ConnectionHandler;
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
        System.out.println("Servidor iniciado na porta: " + port);
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
                //System.out.println("(DEBUG) Server Socket localport: " + serverSocket.getLocalPort() );
                System.out.println("(DEBUG) Socket Local Port: " + socket.getLocalPort() + "| socket port: " + socket.getPort() );
                System.out.println("(3) Nova conexao recebida de: " + socket.getInetAddress().getHostAddress() + ":" + serverSocket.getLocalPort());

                ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());

                Object obj = inputStream.readObject();
                // Cria um novo thread para lidar com a nova conexão
                if (obj instanceof NewConnectionRequest) {
                    NewConnectionRequest request = (NewConnectionRequest) obj;
                    //System.out.println("(DEBUG) Novo pedido de conexao recebido: " + request.toString());
                    node.addConnection(request.getClientAddress(), request.getClientPort(), socket, inputStream, outputStream);

                    if (!node.isAlreadyConnected(request.getClientAddress(), request.getClientPort())) {
                        node.connectToNode(request.getClientAddress().getHostAddress(), request.getClientPort());
                    }
                    
                    new ConnectionHandler(node , socket, inputStream, outputStream).start();

                } else {
                    System.out.println("Objeto recebido não é do tipo NewConnectionRequest ou String.");
                    socket.close();
                }
            }
        } catch (Exception e) {
            System.out.println("Erro ao aceitar nova conexão: " + e.getMessage());
        } 

    }
    
}
