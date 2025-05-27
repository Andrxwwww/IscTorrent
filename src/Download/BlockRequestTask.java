package Download;

import Messages.Connection;
import java.io.Serializable;

/**
 * Representa uma tarefa de solicitação de bloco de arquivo.
 * Esta classe encapsula uma solicitação do bloco e a conexão associada.
 */
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