package GUI;

import Core.Node;
import javax.swing.*;
import java.awt.*;

public class GUINode {
    private JFrame frame;
    private Node node;

    public GUINode(GUI gui) {
        this.node = gui.getNode();
        frame = new JFrame("Add Node");
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        addFrameContent();
        frame.pack();
    }

    public void open() {
        frame.setVisible(true);
    }

    private void addFrameContent() {
        frame.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));

        JLabel addressLabel = new JLabel("Address:");
        JTextField addressField = new JTextField(15);
        addressField.setText(node.getAddress().getHostAddress());
        frame.add(addressLabel);
        frame.add(addressField);

        JLabel portLabel = new JLabel("Port:");
        JTextField portField = new JTextField(5);
        frame.add(portLabel);
        frame.add(portField);

        JButton cancelButton = new JButton("Cancel");
        JButton okButton = new JButton("OK");

        cancelButton.addActionListener(e -> frame.dispose());

        okButton.addActionListener(e -> {
            String address = addressField.getText();
            String portText = portField.getText();
            try {
                int port = Integer.parseInt(portText);
                JOptionPane.showMessageDialog(frame, "Address: " + address + "\nPort: " + port);
                node.connectToNode(address, port);
                frame.dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "Invalid port!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        frame.add(cancelButton);
        frame.add(okButton);
    }
}