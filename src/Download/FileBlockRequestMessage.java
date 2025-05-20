package Download;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FileBlockRequestMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    private int hash;
    private long offset;
    private int length;
    private String senderAddress;
    private int senderPort;

    public FileBlockRequestMessage(int hash, long offset, int length) {
        this.hash = hash;
        this.offset = offset;
        this.length = length;
    }

    public FileBlockRequestMessage(String senderAddress, int senderPort, int hash, long offset, int length) {
        this.hash = hash;
        this.offset = offset;
        this.length = length;
        this.senderAddress = senderAddress;
        this.senderPort = senderPort;
    }

    public void setSenderAddress(String senderAddress) {
        this.senderAddress = senderAddress;
    }

    public void setSenderPort(int senderPort) {
        this.senderPort = senderPort;
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

    public String getSenderAddress() {
        return senderAddress;
    }

    public int getSenderPort() {
        return senderPort;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileBlockRequestMessage that = (FileBlockRequestMessage) o;
        return hash == that.hash && offset == that.offset && length == that.length;
    }

    @Override
    public int hashCode() {
        return Objects.hash(hash, offset, length);
    }

    @Override
    public String toString() {
        String hashString = String.valueOf(hash);
        int length = hashString.length();
        String hashDisplay = length > 10 ? hashString.substring(length - 10) : hashString;
        return "FileBlockRequestMessage [hash=" + hashDisplay + ", offset=" + offset + ", length=" + length + "]";
    }

    public static List<FileBlockRequestMessage> createBlockList(int hash, long fileSize, int blockSize) {
        List<FileBlockRequestMessage> blockList = new ArrayList<>();
        long offset = 0;
        while (offset < fileSize) {
            int length = (int) Math.min(blockSize, fileSize - offset);
            blockList.add(new FileBlockRequestMessage(hash, offset, length));
            offset += length;
        }
        return blockList;
    }
}