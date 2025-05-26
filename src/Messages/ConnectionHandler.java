package Messages;

import Core.Node;
import Download.BlockRequestTask;
import Download.FileBlockAnswerMessage;
import Download.FileBlockRequestMessage;

import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;

public class ConnectionHandler extends Thread {
    private Node node;
    private Socket socket;
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;
    private boolean running = true;

    public ConnectionHandler(Node node, Socket socket, ObjectInputStream input, ObjectOutputStream output) {
        this.node = node;
        this.socket = socket;
        this.inputStream = input;
        this.outputStream = output;
    }

    @Override
    public void run() {
        try {
            while (running && !socket.isClosed()) {
                System.out.println(node.getPortAndAdress() + " Aguardando mensagem de: " + socket.getInetAddress().getHostAddress() + ":" + socket.getPort());
                Object obj = inputStream.readObject();

                if (obj instanceof NewConnectionRequest) {
                    System.out.println(node.getPortAndAdress() + " NewConnectionRequest recebido: " + obj);
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
                } else if (obj instanceof List) {
                    List<FileSearchResult> results = (List<FileSearchResult>) obj;
                    System.out.println(node.getPortAndAdress() + " Recebidos " + results.size() + " resultados de: " + socket.getInetAddress().getHostAddress() + ":" + socket.getPort());
                    node.addSearchResults(results);
                } else if (obj instanceof FileBlockRequestMessage) {
                    FileBlockRequestMessage request = (FileBlockRequestMessage) obj;
                    System.out.println(node.getPortAndAdress() + " Recebido FileBlockRequestMessage: " + request);
                    // Enfileira o pedido em vez de processá-lo diretamente
                    node.addBlockRequest(new BlockRequestTask(request, new Connection(socket, inputStream, outputStream)));
                } else if (obj instanceof FileBlockAnswerMessage) {
                    FileBlockAnswerMessage answer = (FileBlockAnswerMessage) obj;
                    System.out.println(node.getPortAndAdress() + " Recebido FileBlockAnswerMessage: " + answer);
                    node.addReceivedBlock(answer);
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
            node.removeConnection(socket.getInetAddress().getHostAddress(), socket.getPort());
        }
    }

    public ObjectOutputStream getOutputStream() {
        return outputStream;
    }
}