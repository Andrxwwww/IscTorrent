package Core;

import GUI.GUI;
import Messages.Connection;
import Messages.FileSearchResult;
import Messages.NewConnectionRequest;
import Messages.WordSearchMessage;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import Download.*;

public class Node {
    private static final int DEFAULT_PORT = 8080;
    private final int port;
    private final InetAddress address;
    private final GUI gui;
    private final String workFolder;
    private final List<FileSearchResult> localFiles;
    private ServerThread server;
    private final Map<String, Connection> activeConnections;
    private final List<FileBlockAnswerMessage> receivedBlocks;
    private final Map<String, List<FileSearchResult>> fileNameToSearchResults;
    private final BlockingQueue<BlockRequestTask> blockRequestQueue;
    private Thread blockProcessingThread;

    public Node(int nodeID, GUI gui) {
        this.port = DEFAULT_PORT + nodeID;
        this.address = getLocalAddress();
        this.gui = gui;
        this.workFolder = "files/dl" + nodeID;
        this.localFiles = new ArrayList<>();
        this.activeConnections = new HashMap<>();
        this.receivedBlocks = new ArrayList<>();
        this.fileNameToSearchResults = new HashMap<>();
        this.blockRequestQueue = new LinkedBlockingQueue<>();
        startBlockProcessingThread();
        createWorkFolder();
        loadLocalFiles();
        startServing();
    }

        private void startBlockProcessingThread() {
        blockProcessingThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    // Pega o próximo pedido da fila (bloqueia até haver um pedido)
                    BlockRequestTask task = blockRequestQueue.take();
                    FileBlockRequestMessage request = task.getRequest();
                    Connection connection = task.getConnection();

                    // Processa o pedido
                    FileBlockAnswerMessage answer = createBlockAnswer(request);
                    if (answer != null) {
                        synchronized (connection.getOutputStream()) {
                            connection.getOutputStream().reset();
                            connection.getOutputStream().writeObject(answer);
                            connection.getOutputStream().flush();
                            System.out.println(getPortAndAdress() + " Enviado FileBlockAnswerMessage: " + answer + " para " + connection.getSocket().getInetAddress().getHostAddress() + ":" + connection.getSocket().getPort());
                        }
                    } else {
                        System.err.println(getPortAndAdress() + " Não foi possível criar resposta para o pedido: " + request);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.out.println(getPortAndAdress() + " Thread de processamento de blocos interrompida.");
                    break;
                } catch (Exception e) {
                    System.err.println(getPortAndAdress() + " Erro ao processar pedido de bloco: " + e.getMessage());
                }
            }
        });
        blockProcessingThread.start();
    }

    


    public synchronized void addReceivedBlock(FileBlockAnswerMessage answer) {
        receivedBlocks.add(answer);
    }

    public synchronized List<FileBlockAnswerMessage> getReceivedBlocks() {
        return new ArrayList<>(receivedBlocks);
    }

    public synchronized void removeReceivedBlock(FileBlockAnswerMessage answer) {
        receivedBlocks.remove(answer);
    }

    public Connection getConnection(String key) {
        return activeConnections.get(key);
    }

    public void downloadFile(FileSearchResult file) {
        new Thread(() -> new DownloadTaskManager(this, file.getFileName()).startDownload()).start();
    }

    public List<Connection> getPeers() {
        return new ArrayList<>(activeConnections.values());
    }

    private void createWorkFolder() {
        File folder = new File(workFolder);
        if (!folder.exists() && !folder.mkdirs()) {
            throw new RuntimeException("Failed to create directory: " + workFolder);
        }
    }

    public void loadLocalFiles() {
        File folder = new File(workFolder);
        File[] files = folder.listFiles();
        System.out.println("\nNode [" + getPortAndAdress() + "] - ficheiros carregados:");
        if (files != null && files.length > 0) {
            for (File file : files) {
                int hash = calculateFileHash(file);
                FileSearchResult result = new FileSearchResult(file.getName(), hash, file.length(), address, port);
                localFiles.add(result);
                System.out.println(result.toStringFull());
            }
        } else {
            System.out.println("Nenhum ficheiro encontrado nesta diretoria.");
        }
        System.out.println("-------------------------------------------\n");
    }

    public int calculateFileHash(File file) {
        try {
            byte[] fileBytes = Files.readAllBytes(file.toPath());
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(fileBytes);
            return ((hashBytes[0] & 0xFF) << 24) |
                   ((hashBytes[1] & 0xFF) << 16) |
                   ((hashBytes[2] & 0xFF) << 8) |
                   (hashBytes[3] & 0xFF);
        } catch (Exception e) {
            System.err.println("Erro ao calcular hash do arquivo: " + e.getMessage());
            return 0;
        }
    }

    private void startServing() {
        try {
            server = new ServerThread(this, port);
            server.start();
        } catch (Exception e) {
            System.err.println("Erro ao iniciar servidor: " + e.getMessage());
        }
    }

    public void connectToNode(String address, int port) {
        try {
            InetAddress targetAddress = InetAddress.getByName(address);
            if (!isValidConnection(targetAddress, port)) {
                return;
            }
            Socket socket = new Socket(targetAddress, port);
            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
            outputStream.flush();
            Thread.sleep(50); // Pequeno atraso para garantir sincronização
            ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());

            NewConnectionRequest request = new NewConnectionRequest(this.address, this.port);
            synchronized (outputStream) {
                outputStream.reset();
                outputStream.writeObject(request);
                outputStream.flush();
            }
            System.out.println(getPortAndAdress() + " Enviado NewConnectionRequest para: " + targetAddress.getHostAddress() + ":" + port);

            addConnection(targetAddress, port, socket, inputStream, outputStream);
            new Messages.ConnectionHandler(this, socket, inputStream, outputStream).start();
        } catch (IOException | InterruptedException e) {
            System.err.println(getPortAndAdress() + " Erro ao conectar ao nó: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void addConnection(InetAddress address, int port, Socket socket, ObjectInputStream input, ObjectOutputStream output) {
        String key = address.getHostAddress() + ":" + port;
        activeConnections.put(key, new Connection(socket, input, output));
        System.out.println(getPortAndAdress() + " Conexão adicionada: " + key);
    }

    public void removeConnection(String address, int port) {
        String key = address + ":" + port;
        if (activeConnections.remove(key) != null) {
            System.out.println(getPortAndAdress() + " Conexão removida: " + key);
        }
    }

    public void addBlockRequest(BlockRequestTask task) {
        try {
            blockRequestQueue.put(task);
            System.out.println(getPortAndAdress() + " Pedido de bloco adicionado à fila: " + task.getRequest());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println(getPortAndAdress() + " Erro ao adicionar pedido à fila: " + e.getMessage());
        }
    }

    private FileBlockAnswerMessage createBlockAnswer(FileBlockRequestMessage request) {
        File folder = new File(workFolder);
        if (!folder.isDirectory()) return null;
        File[] files = folder.listFiles();
        if (files == null) return null;

        for (File file : files) {
            if (calculateFileHash(file) == request.getHash()) {
                return new FileBlockAnswerMessage(
                    getAddress().getHostAddress(),
                    getPort(),
                    request,
                    file
                );
            }
        }
        return null;
    }

    private boolean isValidConnection(InetAddress targetAddress, int targetPort) {
        if (targetPort < 1024 || targetPort > 65535) {
            System.out.println(getPortAndAdress() + " -> Falha: intervalo de portas inválido");
            return false;
        }
        if (targetAddress.equals(this.address) && targetPort == this.port) {
            System.out.println(getPortAndAdress() + " -> Falha: tentativa de conectar a si próprio");
            return false;
        }
        if (isAlreadyConnected(targetAddress, targetPort)) {
            System.out.println(getPortAndAdress() + " -> Falha: já existe ligação a este nó");
            return false;
        }
        return true;
    }

    public boolean isAlreadyConnected(InetAddress targetAddress, int targetPort) {
        String key = targetAddress.getHostAddress() + ":" + targetPort;
        return activeConnections.containsKey(key);
    }

    public void broadcastSearch(String searchText) {
        System.out.println(getPortAndAdress() + " Iniciando pesquisa por: " + searchText);
        gui.clearSearchResults();
        fileNameToSearchResults.clear(); // Limpa o mapa antes de uma nova pesquisa
        List<Connection> connections = new ArrayList<>(activeConnections.values());
        for (Connection conn : connections) {
            new Thread(() -> {
                try {
                    Socket socket = conn.getSocket();
                    if (socket.isClosed()) {
                        System.err.println(getPortAndAdress() + " Conexão fechada para " + socket.getInetAddress().getHostAddress() + ":" + socket.getPort());
                        removeConnection(socket.getInetAddress().getHostAddress(), socket.getPort());
                        return;
                    }
                    ObjectOutputStream output = conn.getOutputStream();
                    synchronized (output) {
                        output.reset();
                        output.writeObject(new WordSearchMessage(searchText));
                        output.flush();
                    }
                    System.out.println(getPortAndAdress() + " Enviada WordSearchMessage para: " + socket.getInetAddress().getHostAddress() + ":" + socket.getPort());
                } catch (Exception e) {
                    System.err.println(getPortAndAdress() + " Erro ao enviar pesquisa para " + conn.getSocket().getInetAddress().getHostAddress() + ":" + conn.getSocket().getPort() + ": " + e.getMessage());
                    removeConnection(conn.getSocket().getInetAddress().getHostAddress(), conn.getSocket().getPort());
                }
            }).start();
        }
    }

    // Novo método para adicionar resultados de pesquisa ao mapa
    public synchronized void addSearchResults(List<FileSearchResult> results) {
        for (FileSearchResult result : results) {
            fileNameToSearchResults.computeIfAbsent(result.getFileName(), k -> new ArrayList<>()).add(result);
        }
        gui.updateSearchResults(results);
    }

    // Método para obter FileSearchResults por nome de arquivo
    public List<FileSearchResult> getSearchResultsForFile(String fileName) {
        return fileNameToSearchResults.getOrDefault(fileName, new ArrayList<>());
    }

    public List<FileSearchResult> searchLocalFiles(String keyword) {
        List<FileSearchResult> results = new ArrayList<>();
        String search = keyword == null ? "" : keyword.toLowerCase();
        for (FileSearchResult file : localFiles) {
            if (file.getFileName().toLowerCase().contains(search)) {
                results.add(file);
            }
        }
        return results;
    }

    public InetAddress getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    public String getPortAndAdress() {
        return address.getHostAddress() + ":" + port;
    }

    public GUI getGUI() {
        return gui;
    }

    public String getWorkFolder() {
        return workFolder;
    }

    private InetAddress getLocalAddress() {
        try {
            return InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            throw new RuntimeException("Erro ao obter o endereço IP local", e);
        }
    }
}