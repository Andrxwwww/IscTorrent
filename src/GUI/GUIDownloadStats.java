package GUI;

import Core.FileSearchResult;
import Core.Node;
import javax.swing.*;
import java.awt.*;
import java.util.concurrent.TimeUnit;

public class GUIDownloadStats {
    private final GUI gui;
    private final FileSearchResult file;
    private final long durationInMilliseconds;
    private final int blockCount;

    public GUIDownloadStats(GUI gui, FileSearchResult file, long durationInMilliseconds, int blockCount) {
        this.gui = gui;
        this.file = file;
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

        JFrame frame = new JFrame("Download Stats - " + file.getFileName());
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(400, 200);
        frame.setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));

        JLabel downloadFinished = new JLabel("Download concluído: " + file.getFileName());
        downloadFinished.setFont(new Font("Arial", Font.PLAIN, 14));
        frame.add(downloadFinished);

        JLabel nodeLabel = new JLabel(
            String.format("Nó: %s:%d, %d blocos baixados",
                file.getAddress().getHostAddress(), file.getPort(), blockCount)
        );
        nodeLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        frame.add(nodeLabel);

        JLabel timeLabel = new JLabel("Tempo de Download: " + readableTime);
        timeLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        frame.add(timeLabel);

        JLabel speedLabel = new JLabel(
            String.format("Velocidade: %.2f bytes/s", 
                file.getFileSize() / (durationInMilliseconds / 1000.0))
        );
        speedLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        frame.add(speedLabel);

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}