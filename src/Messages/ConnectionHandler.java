package Messages;

import Core.Node;
import Download.FileBlockAnswerMessage;
import Download.FileBlockRequestMessage;
import Core.FileSearchResult;

import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;

/**
 * Thread que trata as conexões com outros nós.
 */
public class ConnectionHandler extends Thread {
    /**
     * Referência ao nó
     */
    private Node node; 
    /**
     * Socket do cliente
     */
    private Socket socket;
    /**
     * Stream de entrada para receber mensagens do cliente
     */
    private ObjectInputStream inputStream;
    /**
     * Stream de saída para enviar mensagens para o cliente
     */
    private ObjectOutputStream outputStream;
    /**
     * Flag para controlar o loop de execução
     */
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
                        node.getGUI().updateSearchResults(results);
                    } else if (obj instanceof FileBlockRequestMessage) {
                        FileBlockRequestMessage request = (FileBlockRequestMessage) obj;
                        System.out.println(node.getPortAndAdress() + " Recebido FileBlockRequestMessage: " + request);
                        FileBlockAnswerMessage answer = createBlockAnswer(request);
                        if (answer != null) {
                            synchronized (outputStream) {
                                outputStream.reset();
                                outputStream.writeObject(answer);
                                outputStream.flush();
                                System.out.println(node.getPortAndAdress() + " Enviado FileBlockAnswerMessage: " + answer);
                            }
                        }
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
            } catch (java.io.InvalidClassException | java.io.StreamCorruptedException e) {
                System.err.println(node.getPortAndAdress() + " Erro de serialização na conexão: " + e.getMessage());
                e.printStackTrace();
            } catch (java.io.EOFException e) {
                System.out.println(node.getPortAndAdress() + " Conexão encerrada pelo outro nó: " + e.getMessage());
            } catch (Exception e) {
                if (running) {
                    System.err.println(node.getPortAndAdress() + " Erro na conexão: " + e.getMessage());
                    e.printStackTrace();
                }
            } finally {
                node.removeConnection(socket.getInetAddress().getHostAddress(), socket.getPort());
            }
        }

        private FileBlockAnswerMessage createBlockAnswer(FileBlockRequestMessage request) {
            File folder = new File(node.getWorkFolder());
            if (!folder.isDirectory()) return null;
            File[] files = folder.listFiles();
            if (files == null) return null;

            for (File file : files) {
                if (node.calculateFileHash(file) == request.getHash()) {
                    return new FileBlockAnswerMessage(
                        node.getAddress().getHostAddress(),
                        node.getPort(),
                        request,
                        file
                    );
                }
            }
            return null;
        }

    /**
     * Método para obter o socket
     * @return Socket do cliente
     */
    public ObjectOutputStream getOutputStream() {
        return outputStream;
    }
}