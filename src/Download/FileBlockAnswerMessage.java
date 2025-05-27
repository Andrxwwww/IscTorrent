package Download;

import java.io.Serializable;
import java.nio.file.Files;

import java.io.File;
import java.io.IOException;

public class FileBlockAnswerMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    private final int hash;
    private final long offset;
    private final int length;
    private final byte[] data;
    private final String senderAddress;
    private final int senderPort;

    public FileBlockAnswerMessage(String senderAddress, int senderPort, FileBlockRequestMessage request, File file) {
        this.hash = request.getHash();
        this.offset = request.getOffset();
        this.length = request.getLength();
        this.senderAddress = senderAddress;
        this.senderPort = senderPort;
        this.data = loadDataFromFile(file);
    }

    // Carrega os dados do bloco a partir do arquivo
    private byte[] loadDataFromFile(File file) {
        try {
            byte[] fileContents = Files.readAllBytes(file.toPath());
            if (offset < 0 || offset + length > fileContents.length) {
                throw new IllegalArgumentException("Invalid offset or length");
            }
            byte[] data = new byte[length];
            System.arraycopy(fileContents, (int) offset, data, 0, length);
            return data;
        } catch (IOException | IllegalArgumentException e) {
            System.err.println("Error reading file block: " + e.getMessage());
            return new byte[0];
        }
    }

    public int getHash() {
        return hash;
    }

    public long getOffset() {
        return offset;
    }

    public int getLength() {
        return length;
    }

    public byte[] getData() {
        return data;
    }

    public String getSenderAddress() {
        return senderAddress;
    }

    public int getSenderPort() {
        return senderPort;
    }

    @Override
    public String toString() {
        return "FileBlockAnswerMessage [hash=" + hash + ", offset=" + offset + ", length=" + length + ", dataSize=" + data.length + "]";
    }
}
