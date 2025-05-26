package Download;

import Core.Node;
import GUI.GUIDownloadStats;
import Messages.Connection;
import Messages.FileSearchResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;

public class DownloadTaskManager {
    private static final int DEFAULT_BLOCK_SIZE = 10240; // 10KB
    private final Node node;
    private final String fileName;
    private final long fileSize;
    private final int fileHash;
    private final List<FileSearchResult> peers;
    private final List<FileBlockRequestMessage> blockRequests;
    private final TreeMap<Long, byte[]> receivedBlocks;
    private final CountDownLatch latch;
    private final Map<String, Integer> blocksPerPeer; // Mapa para rastrear blocos por nó
    private boolean running = true;

    public DownloadTaskManager(Node node, String fileName) {
        this.node = node;
        this.fileName = fileName;
        this.peers = node.getSearchResultsForFile(fileName);
        if (peers.isEmpty()) {
            throw new IllegalStateException("Nenhum nó encontrado para o arquivo: " + fileName);
        }
        this.fileSize = peers.get(0).getFileSize();
        this.fileHash = peers.get(0).getHash();
        this.blockRequests = FileBlockRequestMessage.createBlockList(fileHash, fileSize, DEFAULT_BLOCK_SIZE);
        this.receivedBlocks = new TreeMap<>();
        this.latch = new CountDownLatch(blockRequests.size());
        this.blocksPerPeer = new HashMap<>(); // Inicializa o mapa
        System.out.println(node.getPortAndAdress() + " [DownloadTaskManager] Iniciado para arquivo: " + fileName);
    }

    public void startDownload() {
        try {
            long startTime = System.currentTimeMillis();
            Map<String, Connection> peerConnections = new HashMap<>();
            for (FileSearchResult peer : peers) {
                String peerKey = peer.getAddress().getHostAddress() + ":" + peer.getPort();
                Connection conn = node.getConnection(peerKey);
                if (conn == null || conn.getSocket().isClosed()) {
                    System.err.println(node.getPortAndAdress() + " [DownloadTaskManager] Conexão não encontrada ou fechada para: " + peerKey);
                    continue;
                }
                peerConnections.put(peerKey, conn);
            }

            if (peerConnections.isEmpty()) {
                System.err.println(node.getPortAndAdress() + " [DownloadTaskManager] Nenhuma conexão válida encontrada para o download.");
                return;
            }

            for (int i = 0; i < blockRequests.size(); i++) {
                FileBlockRequestMessage request = blockRequests.get(i);
                String peerKey = peers.get(i % peers.size()).getAddress().getHostAddress() + ":" + peers.get(i % peers.size()).getPort();
                Connection conn = peerConnections.get(peerKey);
                if (conn == null) continue;

                request.setSenderAddress(node.getAddress().getHostAddress());
                request.setSenderPort(node.getPort());

                synchronized (conn.getOutputStream()) {
                    conn.getOutputStream().reset();
                    conn.getOutputStream().writeObject(request);
                    conn.getOutputStream().flush();
                    System.out.println(node.getPortAndAdress() + " [DownloadTaskManager] Enviado pedido de bloco: " + request + " para " + peerKey);
                }
            }

            new Thread(() -> {
                try {
                    while (latch.getCount() > 0 && running) {
                        synchronized (receivedBlocks) {
                            for (FileBlockAnswerMessage answer : node.getReceivedBlocks()) {
                                if (answer.getHash() == fileHash) {
                                    receivedBlocks.put(answer.getOffset(), answer.getData());
                                    // Rastrear o nó fornecedor
                                    String peerKey = answer.getSenderAddress() + ":" + answer.getSenderPort();
                                    blocksPerPeer.merge(peerKey, 1, Integer::sum);
                                    node.removeReceivedBlock(answer);
                                    latch.countDown();
                                    System.out.println(node.getPortAndAdress() + " [DownloadTaskManager] Bloco recebido: " + answer + " de " + peerKey);
                                }
                            }
                        }
                        Thread.sleep(100);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    running = false;
                }
            }).start();

            latch.await();

            if (running && receivedBlocks.size() == blockRequests.size()) {
                writeFile();
                long duration = System.currentTimeMillis() - startTime;
                // Passa a lista de peers e o mapa de blocos por nó
                new GUIDownloadStats(node.getGUI(), fileName, peers, blocksPerPeer, duration, blockRequests.size()).open();
                node.loadLocalFiles();
            } else {
                System.err.println(node.getPortAndAdress() + " [DownloadTaskManager] Download incompleto: " + receivedBlocks.size() + "/" + blockRequests.size() + " blocos recebidos.");
            }

        } catch (Exception e) {
            System.err.println(node.getPortAndAdress() + " [DownloadTaskManager] Erro no download: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void writeFile() {
        try {
            String baseFilePath = node.getWorkFolder() + "/" + fileName;
            String filePath = baseFilePath;
            int suffix = 1;
            while (new java.io.File(filePath).exists()) {
                filePath = baseFilePath.substring(0, baseFilePath.lastIndexOf(".")) + "_" + suffix +
                           baseFilePath.substring(baseFilePath.lastIndexOf("."));
                suffix++;
            }
            byte[] combinedData = new byte[(int) fileSize];
            int position = 0;
            for (byte[] block : receivedBlocks.values()) {
                System.arraycopy(block, 0, combinedData, position, block.length);
                position += block.length;
            }
            Files.write(Paths.get(filePath), combinedData);
            System.out.println(node.getPortAndAdress() + " [DownloadTaskManager] Arquivo escrito: " + filePath);
        } catch (IOException e) {
            System.err.println(node.getPortAndAdress() + " [DownloadTaskManager] Erro ao escrever arquivo: " + e.getMessage());
        }
    }
}