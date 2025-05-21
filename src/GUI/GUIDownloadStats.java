package GUI;

import Core.FileSearchResult;
import Core.Node;
import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class GUIDownloadStats {
    private final GUI gui;
    private final String fileName;
    private final List<FileSearchResult> peers;
    private final Map<String, Integer> blocksPerPeer;
    private final long durationInMilliseconds;
    private final int blockCount;

    public GUIDownloadStats(GUI gui, String fileName, List<FileSearchResult> peers, Map<String, Integer> blocksPerPeer, long durationInMilliseconds, int blockCount) {
        this.gui = gui;
        this.fileName = fileName;
        this.peers = peers;
        this.blocksPerPeer = blocksPerPeer;
        this.durationInMilliseconds = durationInMilliseconds;
        this.blockCount = blockCount;
    }

    private String formatTime(long totalMillis) {
        if (totalMillis < 1000) {
            return String.format("%d ms", totalMillis);
        } else if (totalMillis < 60000) {
            long seconds = totalMillis / 1000;
            long millis = totalMillis % 1000;
            return String.format("%d.%03d seconds", seconds, millis);
        } else {
            long minutes = TimeUnit.MILLISECONDS.toMinutes(totalMillis);
            long seconds = (totalMillis / 1000) % 60;
            long millis = totalMillis % 1000;
            return String.format("%d:%02d.%03d", minutes, seconds, millis);
        }
    }

    public void open() {
        String readableTime = formatTime(durationInMilliseconds);

        JFrame frame = new JFrame("Download Stats - " + fileName);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(400, 200);
        frame.setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));

        JLabel downloadFinished = new JLabel("Download concluído: " + fileName);
        downloadFinished.setFont(new Font("Arial", Font.PLAIN, 14));
        frame.add(downloadFinished);

        // Exibir informações de cada nó fornecedor
        for (FileSearchResult peer : peers) {
            String peerKey = peer.getAddress().getHostAddress() + ":" + peer.getPort();
            int blocks = blocksPerPeer.getOrDefault(peerKey, 0);
            JLabel nodeLabel = new JLabel(
                String.format("Nó: %s:%d, %d blocos baixados", peer.getAddress().getHostAddress(), peer.getPort(), blocks)
            );
            nodeLabel.setFont(new Font("Arial", Font.PLAIN, 14));
            frame.add(nodeLabel);
        }

        JLabel timeLabel = new JLabel("Tempo de Download: " + readableTime);
        timeLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        frame.add(timeLabel);

        JLabel speedLabel = new JLabel(
            String.format("Velocidade: %.2f bytes/s",
                peers.get(0).getFileSize() / (durationInMilliseconds / 1000.0))
        );
        speedLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        frame.add(speedLabel);

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}