package ReadFile;

/**
 *This class represent document from the corpus.
 */
public class Doc {
    private String path;
    private String docNo;
    private  String title;
    private int docSize;
    private String maxfWord;
    private int numOfterms;
    private int maxf;

    /**
     * Constructor
     */
    public Doc() {
    }

    /**
     * Constructor
     * @param path
     * @param docNo
     * @param title
     * @param date
     */
    public Doc( String path, String docNo, String title, String date) {
        this.docNo=docNo;//identifier for document
        this.path = path;
        this.docSize=0;
        this.title=title;
    }

    public String getTitle() {
        return title;
    }

    /**
     * The function set the maxf of the document
     */
    public void setMaxf(int maxf) {
        this.maxf = maxf;
    }

    /**
     * getter
     * @return String
     */
    public int getMaxf() {
        return maxf;
    }

    /**
     * getter
     * @return String
     */
    public String getDocNo() {
        return docNo;
    }

    /**
     * getter
     * @return String
     */
    public String getMaxfWord() {
        return maxfWord;
    }

    /**
     * getter
     * @return int
     */
    public int getNumOfterms() {
        return numOfterms;
    }

    /**
     * The function set the path of the document
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * The function set the docNo of the document
     */
    public void setDocNo(String docNo) {
        this.docNo = docNo;
    }

    /**
     * getter
     * @return String
     */
    public void setDocSize(int docSize) {
        this.docSize = docSize;
    }

    /**
     * The function set the maxf of the word of the document
     */
    public void setMaxfWord(String maxfWord) {
        this.maxfWord = maxfWord;
    }

    /**
     * The function set the terms number of the document
     */
    public void setNumOfterms(int numOfterms) {
        this.numOfterms = numOfterms;
    }

    /**
     * getter
     * @return int
     */
    public int getDocSize() {
        return docSize;
    }

    /**
     *.אThe function set the size of the document
     */
    public void setDocSize() {
        this.docSize++;
    }

    /**
     * getter
     * @return String
     */
    public String getPath() {
        return path;
    }

    /**
     * getter
     * @return String
     */
    public String getDocID() {
        return docNo;
    }

    /**
     * override the tostring method, tostring all the doc info
     * @return String
     */
    @Override
    public String toString() {
        return this.docNo+"|"+this.docSize+"|"+this.maxf+"|"+this.maxfWord+"|"+this.title+"|"+this.numOfterms;
    }
}
