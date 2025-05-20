package GUI;

import Core.FileSearchResult;
import Core.Node;
import Download.FileBlockRequestMessage;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class GUI {
    private JFrame frame;
    private Node node;
    private DefaultListModel<FileSearchResult> listModel;

    public GUI(int nodeID) {
        this.node = new Node(nodeID, this);
        createGUI();
    }

    private void createGUI() {
        frame = new JFrame("Node - " + node.getPortAndAdress());
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField searchField = new JTextField(20);
        JButton searchButton = new JButton("Search");

        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);

        frame.add(searchPanel, BorderLayout.NORTH);

        listModel = new DefaultListModel<>();
        JList<FileSearchResult> resultList = new JList<>(listModel);
        JScrollPane scrollPane = new JScrollPane(resultList);
        scrollPane.setPreferredSize(new Dimension(300, 200));

        JPanel buttonPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        JButton downloadButton = new JButton("Download");
        JButton connectButton = new JButton("Connect to Node");

        buttonPanel.add(downloadButton);
        buttonPanel.add(connectButton);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        centerPanel.add(buttonPanel, BorderLayout.EAST);

        frame.add(centerPanel, BorderLayout.CENTER);

        searchButton.addActionListener(e -> {
            String keyword = searchField.getText();
            node.broadcastSearch(keyword);
        });

        connectButton.addActionListener(e -> {
            GUINode connectWindow = new GUINode(this);
            connectWindow.open();
        });

        downloadButton.addActionListener(e -> {
            int selectedIndex = resultList.getSelectedIndex();
            if (selectedIndex == -1) {
                JOptionPane.showMessageDialog(frame, "Seleciona um ficheiro primeiro.");
                return;
            }
            FileSearchResult selectedFile = listModel.getElementAt(selectedIndex);
            List<FileBlockRequestMessage> blocos = FileBlockRequestMessage.createBlockList(
                selectedFile.getHash(), selectedFile.getFileSize(), 10240
            );
            System.out.println(" - Blocos para o ficheiro: " + selectedFile.getFileName());
            for (FileBlockRequestMessage bloco : blocos) {
                System.out.println(bloco);
            }
            System.out.println(" - Total de blocos: " + blocos.size());
        });

        frame.pack();
        frame.setLocationRelativeTo(null);
    }

    public void open() {
        frame.setVisible(true);
    }

    public Node getNode() {
        return node;
    }

    public void clearSearchResults() {
        SwingUtilities.invokeLater(() -> listModel.clear());
    }

    public void updateSearchResults(List<FileSearchResult> results) {
        SwingUtilities.invokeLater(() -> {
            for (FileSearchResult result : results) {
                if (!listModel.contains(result)) {
                    listModel.addElement(result);
                }
            }
        });
    }
}