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

    public FileSearchResult(String fileName, int hash, long fileSize, InetAddress address, int port) {
        this.fileName = fileName;
        this.hash = hash;
        this.fileSize = fileSize;
        this.address = address;
        this.port = port;
    }

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

    public String toStringFull() {
        return fileName + " [" + fileSize + " bytes] - :" + port + " - " + hash;
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