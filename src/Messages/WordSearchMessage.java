package Messages;


import java.io.Serializable;

public class WordSearchMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    private String keyword;

    public WordSearchMessage(String keyword) {
        this.keyword = keyword;
    }

    public String getKeyword() {
        return keyword;
    }

    @Override
    public String toString() {
        return "WordSearchMessage [keyword=" + keyword + "]";
    }
}
