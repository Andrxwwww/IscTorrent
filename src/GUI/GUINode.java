package GUI;

import Core.Node;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.net.InetAddress;

public class GUINode {
    
    private JFrame frame;
    private Node node;

    /**
     * Construtor para a classe GUI.
     * @param gui GUI associada ao nó.
     */
    public GUINode(GUI gui) {
        this.node = gui.getNode();
        frame = new JFrame("Connect to Node");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setupNodeGUI();
        frame.pack();
    }

    /**
     * Configura a GUI do nó.
     */
    private void setupNodeGUI() {
        frame.setLayout(new FlowLayout());

        JTextField addressField = new JTextField( getHostInString() ,15);
        JTextField portField = new JTextField( 5);

        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("Cancel");

        okButton.addActionListener((ActionEvent e) -> {
            try {
                String address = addressField.getText();
                int port = Integer.parseInt(portField.getText());
                node.connectToNode(address, port);
                frame.dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "Invalid port number.");
            }
        });

        cancelButton.addActionListener(e -> frame.dispose());

        frame.add(new JLabel("Address:"));
        frame.add(addressField);
        frame.add(new JLabel("Port:"));
        frame.add(portField);
        frame.add(okButton);
        frame.add(cancelButton);
    }

    /**
     * Abre a janela da GUI.
     */
    public void open() {
        frame.setVisible(true);
    }

    /**
     * Retorna o endereço IP local do nó.
     * @return Endereço IP local do nó.
     * @throws UnknownHostException se ocorrer um erro ao obter o endereço IP local.
     */
    public String getHostInString() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
