package Download;

import Messages.Connection;
import java.io.Serializable;

public class BlockRequestTask implements Serializable {
    private final FileBlockRequestMessage request;
    private final Connection connection;

    public BlockRequestTask(FileBlockRequestMessage request, Connection connection) {
        this.request = request;
        this.connection = connection;
    }

    public FileBlockRequestMessage getRequest() {
        return request;
    }

    public Connection getConnection() {
        return connection;
    }
}