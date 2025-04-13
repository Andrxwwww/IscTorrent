package Download;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Representa um pedido de um bloco específico de um ficheiro.
 */
public class FileBlockRequestMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    private String filename;  
    private long offset;      
    private int length;       

    public FileBlockRequestMessage(String filename, long offset, int length) {
        this.filename = filename;
        this.offset = offset;
        this.length = length;
    }

    public String getFilename() {
        return filename;
    }

    public long getOffset() {
        return offset;
    }

    public int getLength() {
        return length;
    }

    @Override
    public String toString() {
        return "FileBlockRequestMessage [filename=" + filename +
               ", offset=" + offset +
               ", length=" + length + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FileBlockRequestMessage)) return false;
        FileBlockRequestMessage that = (FileBlockRequestMessage) o;
        return offset == that.offset &&
               length == that.length &&
               Objects.equals(filename, that.filename);
    }

    @Override
    public int hashCode() {
        return Objects.hash(filename, offset, length);
    }

    /**
     * Cria uma lista de blocos a partir do tamanho total do ficheiro.
     * Cada bloco terá no máximo o tamanho definido por blockSize.
     *
     * @param filename  nome do ficheiro
     * @param fileSize  tamanho total do ficheiro
     * @param blockSize tamanho máximo de cada bloco (em bytes)
     * @return lista de mensagens de pedido de blocos
     */
    public static List<FileBlockRequestMessage> createBlockList( String filename, long fileSize, int blockSize) {

        List<FileBlockRequestMessage> blockList = new ArrayList<>();
        long offset = 0;

        while (offset < fileSize) {
            int length = (int) Math.min(blockSize, fileSize - offset);
            blockList.add(new FileBlockRequestMessage(filename, offset, length));
            offset += length;
        }

        return blockList;
    }
}

