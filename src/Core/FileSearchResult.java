package Core;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.Objects;

public class FileSearchResult implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private String fileName;
    private int hash;
    private long fileSize;
    private InetAddress address;
    private int port;

    /**
     * Construtor adaptado simplificado para uso atual.
     * @param fileName Nome do ficheiro.
     * @param hash Hash identificadora do ficheiro.
     * @param fileSize Tamanho do ficheiro.
     * @param address Endereço do nó que detém o ficheiro.
     * @param port Porta do nó que detém o ficheiro.
     */
    public FileSearchResult(String fileName, int hash, long fileSize, InetAddress address, int port) {
        this.fileName = fileName;
        this.hash = hash;
        this.fileSize = fileSize;
        this.address = address;
        this.port = port;
    }

    // Getters necessários
    public String getFileName() {
        return fileName;
    }

    public int getHash() {
        return hash;
    }

    public long getFileSize() {
        return fileSize;
    }

    public InetAddress getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    @Override
    public String toString() {
        return fileName + " [" + fileSize + " bytes]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileSearchResult that = (FileSearchResult) o;
        return hash == that.hash;
    }

    @Override
    public int hashCode() {
        return Objects.hash(hash);
    }
}
