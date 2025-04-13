package GUI;

import java.awt.*;
import java.io.File;

import javax.swing.*;

import Core.FileSearchResult;
import Core.Node;
import Download.FileBlockRequestMessage;

import java.util.List;

public class GUI {

    private JFrame frame;
    private Node node;
    private DefaultListModel<FileSearchResult> listModel;

    /**
     * Construtor para a classe GUI.
     * @param nodeID Identificador único para o nó.
     */
    public GUI(int nodeID){
        this.node = new Node(nodeID,this);
        createGUI();
        loadLocalFilesIntoGUI();
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
        listModel = new DefaultListModel<>();
        JList<FileSearchResult> resultList = new JList<>(listModel);
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
        
            // Criar blocos a partir do ficheiro selecionado
            List<FileBlockRequestMessage> blocos = FileBlockRequestMessage.createBlockList(selectedFile.getFileName(),selectedFile.getFileSize(), 10240 // 10KB por bloco
            );
        
            // Imprimir os blocos na consola
            System.out.println(" - Blocos para o ficheiro: " + selectedFile.getFileName());
            for (FileBlockRequestMessage bloco : blocos) {
                System.out.println(bloco);
            }
            System.out.println(" - Total de blocos: " + blocos.size());
        });

        frame.pack();
        frame.setLocationRelativeTo(null);
    }

    /**
     * Carrega os ficheiros locais do Node na GUI.
     */
    public void loadLocalFilesIntoGUI() {
        List<FileSearchResult> localFiles = node.getLocalFiles();
        listModel.clear();
        for(FileSearchResult result : localFiles){
            listModel.addElement(result);
        }
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
