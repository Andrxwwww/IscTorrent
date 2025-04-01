package Main;

import java.net.InetAddress;
import java.net.UnknownHostException;

import GUI.GUI;

public class IscTorrent {
    public static void main(String[] args) throws UnknownHostException {

        if (args.length < 1){
            System.out.println("Necessário passar o número da porta como argumento.");
            return;
        }

        try {
            int nodeID = Integer.parseInt(args[0]);
            GUI gui = new GUI(nodeID);
            gui.open();
        } catch (NumberFormatException e) {
            System.out.println("Número da porta inválido. Por favor, forneça um número inteiro.");
        }
    }
}
