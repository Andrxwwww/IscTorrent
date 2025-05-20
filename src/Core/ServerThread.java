package Core;

import Messages.NewConnectionRequest;
import Messages.ConnectionHandler;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerThread extends Thread {
    /**
     * referencia ao node
     */
    private Node node; 

    /**
     * referencia ao socket do servidor onde aceita as conexões
     */
    private ServerSocket serverSocket;

    public ServerThread(Node node, int port) throws Exception {
        this.node = node;
        this.serverSocket = new ServerSocket(port);
        System.out.println(node.getPortAndAdress() + " Servidor iniciado na porta: " + port);
    }

    /**
     * Método que inicia o servidor e aceita novas conexões
     */
    @Override
    public void run() {
        try {
            // Loop infinito para aceitar novas conexões
            while (true) {
                // Aceita uma nova conexão de um cliente
                Socket socket = serverSocket.accept();
                System.out.println(node.getPortAndAdress() + " Nova conexão recebida de: " + socket.getInetAddress().getHostAddress() + ":" + socket.getPort());

                // Cria o stram de saida para enviar mensagens para o cliente
                ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
                outputStream.flush(); // Flush para garantir que o cabeçalho seja enviado antes de qualquer outro dado

                // Cria o stram de entrada para enviar mensagens para o cliente
                ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());

                // Aguarda a leitura de um objeto do cliente
                Object obj = inputStream.readObject();

                // Verifica se o objeto recebido é do tipo NewConnectionRequest
                if (obj instanceof NewConnectionRequest) {
                    NewConnectionRequest request = (NewConnectionRequest) obj;
                    System.out.println(node.getPortAndAdress() + " Novo pedido de conexão recebido: " + request.toString());

                    // Adiciona a conexão ao nó
                    node.addConnection(request.getClientAddress(), request.getClientPort(), socket, inputStream, outputStream);

                    // Inicia uma nova thread de conexão para gerenciar a comunicação com o cliente
                    new ConnectionHandler(node, socket, inputStream, outputStream).start();

                    // Verifica se o nó já está conectado ao cliente
                    if (!node.isAlreadyConnected(request.getClientAddress(), request.getClientPort())) {
                        System.out.println(node.getPortAndAdress() + " Estabelecendo conexão de volta para: " + request.getClientAddress().getHostAddress() + ":" + request.getClientPort());

                        // Se não estiver conectado, tenta conectar ao cliente
                        node.connectToNode(request.getClientAddress().getHostAddress(), request.getClientPort());
                    }
                } else {
                    System.err.println(node.getPortAndAdress() + " Objeto recebido não é do tipo NewConnectionRequest: " + obj);
                    // Se o objeto não for do tipo NewConnectionRequest, fecha a conexão
                    socket.close();
                }
            }
        } catch (Exception e) {
            System.err.println(node.getPortAndAdress() + " Erro ao aceitar nova conexão: " + e.getMessage());
            e.printStackTrace();
        }
    }
}