package Download;

import Core.FileSearchResult;
import Core.Node;
import GUI.GUIDownloadStats;
import Messages.Connection;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import java.util.List;
import java.util.TreeMap;

public class DownloadTaskManager {
    private static final int DEFAULT_BLOCK_SIZE = 10240; // 10KB
    private final Node node;
    private final FileSearchResult fileSearchResult;
    private final List<FileBlockRequestMessage> blockRequests;
    private final TreeMap<Long, byte[]> receivedBlocks;
    private boolean running = true;

    public DownloadTaskManager(Node node, FileSearchResult fileSearchResult) {
        this.node = node;
        this.fileSearchResult = fileSearchResult;
        this.blockRequests = FileBlockRequestMessage.createBlockList(
            fileSearchResult.getHash(), fileSearchResult.getFileSize(), DEFAULT_BLOCK_SIZE
        );
        this.receivedBlocks = new TreeMap<>();
        System.out.println(node.getPortAndAdress() + " [DownloadTaskManager] Iniciado para arquivo: " + fileSearchResult.getFileName());
    }

    public void startDownload() {
        try {
            String peerKey = fileSearchResult.getAddress().getHostAddress() + ":" + fileSearchResult.getPort();
            Connection conn = node.getConnection(peerKey);
            if (conn == null || conn.getSocket().isClosed()) {
                System.err.println(node.getPortAndAdress() + " [DownloadTaskManager] Conexão não encontrada ou fechada para: " + peerKey);
                return;
            }

            long startTime = System.currentTimeMillis();
            for (FileBlockRequestMessage request : blockRequests) {
                if (!running) {
                    System.out.println(node.getPortAndAdress() + " [DownloadTaskManager] Download interrompido.");
                    break;
                }
                request.setSenderAddress(node.getAddress().getHostAddress());
                request.setSenderPort(node.getPort());

                synchronized (conn.getOutputStream()) {
                    conn.getOutputStream().reset();
                    conn.getOutputStream().writeObject(request);
                    conn.getOutputStream().flush();
                    System.out.println(node.getPortAndAdress() + " [DownloadTaskManager] Enviado pedido de bloco: " + request);
                }

                FileBlockAnswerMessage answer = waitForAnswer(request, 10000);
                if (answer == null || answer.getData().length == 0) {
                    System.err.println(node.getPortAndAdress() + " [DownloadTaskManager] Falha ao receber bloco: " + request);
                    running = false;
                    break;
                }
                receivedBlocks.put(answer.getOffset(), answer.getData());
                System.out.println(node.getPortAndAdress() + " [DownloadTaskManager] Bloco recebido: " + answer);
            }

            if (running && receivedBlocks.size() == blockRequests.size()) {
                writeFile();
                long duration = System.currentTimeMillis() - startTime;
                new GUIDownloadStats(node.getGUI(), fileSearchResult, duration, blockRequests.size()).open();
                node.loadLocalFiles();
            }

        } catch (Exception e) {
            System.err.println(node.getPortAndAdress() + " [DownloadTaskManager] Erro no download: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private FileBlockAnswerMessage waitForAnswer(FileBlockRequestMessage request, long timeoutMs) {
        long endTime = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < endTime) {
            synchronized (receivedBlocks) {
                for (FileBlockAnswerMessage answer : node.getReceivedBlocks()) {
                    if (answer.getHash() == request.getHash() && answer.getOffset() == request.getOffset()) {
                        node.removeReceivedBlock(answer);
                        return answer;
                    }
                }
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            }
        }
        return null;
    }

    private void writeFile() {
        try {
            String baseFilePath = node.getWorkFolder() + "/" + fileSearchResult.getFileName();
            String filePath = baseFilePath;
            int suffix = 1;
            while (new java.io.File(filePath).exists()) {
                filePath = baseFilePath.substring(0, baseFilePath.lastIndexOf(".")) + "_" + suffix +
                           baseFilePath.substring(baseFilePath.lastIndexOf("."));
                suffix++;
            }
            byte[] combinedData = new byte[(int) fileSearchResult.getFileSize()];
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