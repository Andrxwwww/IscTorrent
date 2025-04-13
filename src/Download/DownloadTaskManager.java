package Download;

import java.io.File;
import java.util.List;

import Core.FileSearchResult;

public class DownloadTaskManager {

    private static final int DEFAULT_BLOCK_SIZE = 10240; // 10KB

    private FileSearchResult fileSearchResult;
    private List<FileBlockRequestMessage> blockRequests;

    /**
     * Construtor para a classe DownloadTaskManager.
     * @param fileSearchResult O resultado da pesquisa de ficheiro.
     */
    public DownloadTaskManager(FileSearchResult fileSearchResult) {
        this.fileSearchResult = fileSearchResult;
        generateBlockList();
    }

    /**
     * Cria uma lista de blocos a partir do tamanho total do ficheiro.
     * @return Lista de blocos.
     */
    private void generateBlockList(){
        blockRequests = FileBlockRequestMessage.createBlockList(fileSearchResult.getFileName() ,fileSearchResult.getFileSize() , DEFAULT_BLOCK_SIZE);
        System.out.println("Block list generated for file: " + fileSearchResult.getFileName() + " with size: " + fileSearchResult.getFileSize() + " bytes." + "Number of blocks: " + blockRequests.size());
    }

    
}
