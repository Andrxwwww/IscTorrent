package Messages;

import Core.Node;
import Download.BlockRequestTask;
import Download.FileBlockAnswerMessage;
import Download.FileBlockRequestMessage;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;

public class ConnectionHandler extends Thread {
    private Node node; // Referência ao nó associado à conexão
    private Socket socket; // Socket da conexão com o cliente
    private ObjectInputStream inputStream; // Stream de entrada para receber mensagens
    private ObjectOutputStream outputStream; // Stream de saída para enviar mensagens
    private boolean running = true; // Flag que indica se a thread está em execução

    public ConnectionHandler(Node node, Socket socket, ObjectInputStream input, ObjectOutputStream output) {
        this.node = node;
        this.socket = socket;
        this.inputStream = input;
        this.outputStream = output;
    }

    // Executa o loop principal para receber e processar mensagens
    @Override
    public void run() {
        try {
            while (running && !socket.isClosed()) {
                // Aguarda uma mensagem do cliente
                System.out.println(node.getPortAndAdress() + " Aguardando mensagem de: " + socket.getInetAddress().getHostAddress() + ":" + socket.getPort());
                Object obj = inputStream.readObject();

                // Processa um pedido de nova conexão
                if (obj instanceof NewConnectionRequest) {
                    System.out.println(node.getPortAndAdress() + " NewConnectionRequest recebido: " + obj);

                // Processa uma mensagem de pesquisa de arquivos
                } else if (obj instanceof WordSearchMessage) {
                    WordSearchMessage searchMessage = (WordSearchMessage) obj;
                    System.out.println(node.getPortAndAdress() + " Pesquisa recebida: " + searchMessage);
                    List<FileSearchResult> results = node.searchLocalFiles(searchMessage.getKeyword());
                    synchronized (outputStream) {
                        outputStream.reset();
                        outputStream.writeObject(results);
                        outputStream.flush();
                    }
                    System.out.println(node.getPortAndAdress() + " Enviados " + results.size() + " resultados para " + socket.getInetAddress().getHostAddress() + ":" + socket.getPort());
                    
                 // Processa resultados de pesquisa recebidos
                } else if (obj instanceof List) {
                    List<FileSearchResult> results = (List<FileSearchResult>) obj;
                    System.out.println(node.getPortAndAdress() + " Recebidos " + results.size() + " resultados de: " + socket.getInetAddress().getHostAddress() + ":" + socket.getPort());
                    node.addSearchResults(results);

                // Processa um pedido de bloco de arquivo
                } else if (obj instanceof FileBlockRequestMessage) {
                    FileBlockRequestMessage request = (FileBlockRequestMessage) obj;
                    System.out.println(node.getPortAndAdress() + " Recebido FileBlockRequestMessage: " + request);
                    // enqueue o pedido em vez de processá-lo diretamente (era como tinha antes)
                    node.addBlockRequest(new BlockRequestTask(request, new Connection(socket, inputStream, outputStream)));

                // Processa uma resposta de bloco de arquivo
                } else if (obj instanceof FileBlockAnswerMessage) {
                    FileBlockAnswerMessage answer = (FileBlockAnswerMessage) obj;
                    System.out.println(node.getPortAndAdress() + " Recebido FileBlockAnswerMessage: " + answer);
                    node.addReceivedBlock(answer);

                // Lida com mensagens desconhecidas
                } else {
                    System.err.println(node.getPortAndAdress() + " Mensagem desconhecida recebida: " + obj);
                }
            }
        } catch (SocketException e) {
            System.out.println(node.getPortAndAdress() + " Conexão resetada pelo outro nó: " + socket.getInetAddress().getHostAddress() + ":" + socket.getPort());
        } catch (Exception e) {
            if (running) {
                System.err.println(node.getPortAndAdress() + " Erro na conexão: " + e.getMessage());
                e.printStackTrace();
            }
        } finally {
            // Remove a conexão do nó ao finalizar se nao estiver a correr ou se a conexão for fechada
            node.removeConnection(socket.getInetAddress().getHostAddress(), socket.getPort());
        }
    }

    // Retorna o stream de saída da conexão
    public ObjectOutputStream getOutputStream() {
        return outputStream;
    }
}