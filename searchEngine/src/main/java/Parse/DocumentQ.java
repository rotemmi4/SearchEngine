package Parse;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Represent a document that return from the Ranker.
 */
public class DocumentQ implements Comparable{
    private String DocNo;
    private int size;
    private HashMap<String,Integer> termsAndFreq;//<term,freq>
    private double rank;
    private HashSet<String> wordsInTitle;
    private int queryTermandDocTerm;


    /**
     * constructor
     * @param DocNo
     */
    public DocumentQ(String DocNo) {
        this.DocNo=DocNo;
        this.termsAndFreq = new HashMap<String, Integer>();
        this.rank=0;
        this.size=0;
        this.wordsInTitle=new HashSet<String>();
        queryTermandDocTerm=0;
    }

    /**
     * getter
     * @return
     */
    public HashSet<String> getWordsInTitle() {
        return wordsInTitle;
    }

    /**
     * get the document's title and parse it.
     * @param wordsInTitle
     * @param isStem
     * @param stopWords
     * @throws IOException
     */
    public void setWordsInTitle(String wordsInTitle, boolean isStem, HashSet<String> stopWords) throws IOException {
        Parse parse=new Parse(wordsInTitle,isStem);
        parse.parseText(-2,stopWords);
        HashMap<String,Integer> title=parse.getTokens();
        for (String word: title.keySet()) {
            this.wordsInTitle.add(word);
        }
    }

    public void setQueryTermandDocTerm() {
        this.queryTermandDocTerm ++;
    }

    /**
     * add terms from document and his frequency.
     * @param term
     * @param freq
     */
    public void addTermsAndFreq(String term, int freq) {
        this.termsAndFreq.put(term,freq);
    }

    /**
     * setter
     * @param rank
     */
    public void setRank(double rank) {
        this.rank = rank;
    }

    /**
     * getter
     * @return
     */
    public int getSize() {
        return size;
    }

    /**
     * getter
     * @return
     */
    public String getDocNo() {
        return DocNo;
    }

    /**
     * getter
     * @return
     */
    public void setSize(int size) {
        this.size = size;
    }

    /**
     * getter
     * @return
     */
    public HashMap<String, Integer> getTermsAndFreq() {
        return termsAndFreq;
    }

    public int getQueryTermandDocTerm() {
        return queryTermandDocTerm;
    }

    /**
     * getter
     * @return
     */
    public double getRank() {
        return rank;
    }

    /**
     * Override the method "compareTo" by the rank
     * @param o
     * @return
     */
    @Override
    public int compareTo(Object o) {
        if (((DocumentQ)o).rank<this.rank)
            return -1;
        if (((DocumentQ)o).rank>this.rank)
            return 1;
        if (((DocumentQ)o).rank==this.rank)
            return 0;
        return 0;
    }


}
