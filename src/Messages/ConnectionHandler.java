package Messages;

import Core.Node;
import Core.FileSearchResult;
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
            // Loop para aguardar mensagens do cliente
            while (running && !socket.isClosed()) {
                System.out.println(node.getPortAndAdress() + " Aguardando mensagem de: " + socket.getInetAddress().getHostAddress() + ":" + socket.getPort());

                // Aguarda a leitura de um objeto do cliente
                Object obj = inputStream.readObject();

                // Verifica o tipo do objeto recebido e processa conforme necessário
                if (obj instanceof NewConnectionRequest) {
                    System.out.println(node.getPortAndAdress() + " NewConnectionRequest recebido: " + obj);
                    
                // Verifica se o objeto é uma mensagem de pesquisa    
                } else if (obj instanceof WordSearchMessage) {
                    WordSearchMessage searchMessage = (WordSearchMessage) obj;
                    System.out.println(node.getPortAndAdress() + " Pesquisa recebida: " + searchMessage);

                    // Realiza a pesquisa local e envia os resultados de volta
                    List<FileSearchResult> results = node.searchLocalFiles(searchMessage.getKeyword());

                    // Envia os resultados encontrados para o cliente
                    synchronized (outputStream) {
                        outputStream.reset(); // Limpa o cache do stream
                        outputStream.writeObject(results); // Envia a lista de resultados
                        outputStream.flush(); // Garante que os dados sejam enviados imediatamente
                    }
                    System.out.println(node.getPortAndAdress() + " Enviados " + results.size() + " resultados para " + socket.getInetAddress().getHostAddress() + ":" + socket.getPort());

                    // Verifica se o objeto é uma lista de mensagensp de resultado de pesquisa
                } else if (obj instanceof List) {
                    // Verifica se a lista contém objetos do tipo FileSearchResult
                    List<FileSearchResult> results = (List<FileSearchResult>) obj;
                    System.out.println(node.getPortAndAdress() + " Recebidos " + results.size() + " resultados de: " + socket.getInetAddress().getHostAddress() + ":" + socket.getPort());

                    // Atualiza a GUI com os resultados recebidos
                    node.getGUI().updateSearchResults(results);
                } else {
                    System.err.println(node.getPortAndAdress() + " Mensagem desconhecida recebida: " + obj);
                }
            }
        } catch (SocketException e) {
            // Exceção lançada quando a conexão é resetada pelo outro nó
            System.out.println(node.getPortAndAdress() + " Conexão resetada pelo outro nó: " + socket.getInetAddress().getHostAddress() + ":" + socket.getPort());
        } catch (java.io.InvalidClassException | java.io.StreamCorruptedException e) {
            // Exceção lançada quando há erro de serialização
            System.err.println(node.getPortAndAdress() + " Erro de serialização na conexão: " + e.getMessage());
            e.printStackTrace();
        } catch (java.io.EOFException e) {
            // Exceção lançada quando o outro nó fecha a conexão
            System.out.println(node.getPortAndAdress() + " Conexão encerrada pelo outro nó: " + e.getMessage());
        } catch (Exception e) {
            if (running) {
                System.err.println(node.getPortAndAdress() + " Erro na conexão: " + e.getMessage());
                e.printStackTrace();
            }
        } finally {
            close();
            // Remove a conexão do nó após o fechamento
            node.removeConnection(socket.getInetAddress().getHostAddress(), socket.getPort());
        }
    }

    /**
     * Método para fechar a conexão
     */
    public void close() {
        if (!running) return;
        running = false;
        try {
            if (inputStream != null) inputStream.close();
            if (outputStream != null) outputStream.close();
            if (socket != null && !socket.isClosed()) socket.close();
            System.out.println(node.getPortAndAdress() + " Conexão fechada para: " + socket.getInetAddress().getHostAddress() + ":" + socket.getPort());
        } catch (Exception e) {
            System.err.println(node.getPortAndAdress() + " Erro ao fechar socket: " + e.getMessage());
        }
    }

    /**
     * Método para obter o socket
     * @return Socket do cliente
     */
    public ObjectOutputStream getOutputStream() {
        return outputStream;
    }
}