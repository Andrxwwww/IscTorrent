package GUI;

import java.awt.*;
import javax.swing.*;

import Core.Node;

public class GUI {
    private JFrame frame;
    private Node node;


    /**
     * Construtor para a classe GUI.
     * @param nodeID Identificador único para o nó.
     */
    public GUI(int nodeID){
        this.node = new Node(nodeID,this);
        createGUI();
    }

    /**
     * Configura a GUI do nó.
     */
    private void createGUI(){
        frame = new JFrame("Node - " + node.getPortAndAdress());
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // Painel de pesquisa
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField searchField = new JTextField(20);
        JButton searchButton = new JButton("Search");

        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);

        frame.add(searchPanel, BorderLayout.NORTH);

        // Área de resultados
        DefaultListModel<String> listModel = new DefaultListModel<>();
        JList<String> resultList = new JList<>(listModel);
        JScrollPane scrollPane = new JScrollPane(resultList);
        scrollPane.setPreferredSize(new Dimension(300, 200));

        // Botões laterais
        JPanel buttonPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        JButton downloadButton = new JButton("Download");
        JButton connectButton = new JButton("Connect to Node");

        buttonPanel.add(downloadButton);
        buttonPanel.add(connectButton);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        centerPanel.add(buttonPanel, BorderLayout.EAST);

        frame.add(centerPanel, BorderLayout.CENTER);

        // Listeners
        searchButton.addActionListener(e -> {
            String keyword = searchField.getText();
            node.broadcastSearch(keyword); // Simulado
            listModel.clear();
            listModel.addElement("Fake result for: " + keyword);
        });

        connectButton.addActionListener(e -> {
            GUINode connectWindow = new GUINode(this);
            connectWindow.open();
        });

        frame.pack();
        frame.setLocationRelativeTo(null);
    }

    /**
     * Abre a janela da GUI.
     */
    public void open() {
        frame.setVisible(true);
    }

    /**
     * @return o node associado à GUI.
     */
    public Node getNode() {
        return node;
    }
}
