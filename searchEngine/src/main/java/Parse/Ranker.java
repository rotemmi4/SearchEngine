package Parse;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;

/**
 * Rank the documents for query.
 */
public class Ranker {
    private PriorityQueue<DocumentQ> DocsRank; //<DocNo,rank>
    private HashMap<String,DocumentQ> docsInfo;
    private HashMap<String,Integer> termFreq;
    private int numOfDocs=0;
    private double avgOfDocsSize=0;
    private boolean isStem;
    private HashSet<String> stopWords;
    private Indexer indexer=Indexer.getInstance();


    /**
     * constructor
     * @param docsInfo
     * @param termFreq
     * @param path
     * @param isStem
     * @param stopWords
     */
    public Ranker(HashMap<String,DocumentQ> docsInfo, HashMap<String,Integer> termFreq, String path, boolean isStem, HashSet<String> stopWords) {
        DocsRank=new PriorityQueue<DocumentQ>();
        this.docsInfo=docsInfo;
        this.termFreq=termFreq;
        this.isStem=isStem;
        this.stopWords=stopWords;
        readDocsDictionary(path);
    }

    /**
     * rank the document and return the 50 documents with the highest rank.
     * @return
     */
    public PriorityQueue<DocumentQ> RankQuery(HashMap<String, Integer> queryTerms){
        BM25Algorithm(queryTerms);
        get50RelevantDocs();
        if(this.DocsRank.size()>50){
            get50RelevantDocs();
        }
        return this.DocsRank;
    }

    /**
     * BM25 Algorithm.
     * the algorithm get biggest weight to words that show in document's title.
     */
    private void BM25Algorithm(HashMap<String, Integer> queryTerms){
        double k=1.25;
        double b=0.5;
        for (String doc:docsInfo.keySet()) {
            int D=this.docsInfo.get(doc).getSize();
            double rankDoc=0;
            for (String term:this.docsInfo.get(doc).getTermsAndFreq().keySet()) {
                //few query term in the doc
                if(queryTerms.containsKey(term)){
                    this.docsInfo.get(doc).setQueryTermandDocTerm();
                }
                double fq=this.docsInfo.get(doc).getTermsAndFreq().get(term);
                //semantic
                if(queryTerms.containsKey(term) && queryTerms.get(term)==-1){
                    fq=fq*0.5;
                }
                //header
                if(this.docsInfo.get(doc).getWordsInTitle().contains(term) && queryTerms.get(term)!=-1){
                    fq=fq*1.4;
                }
                //entity
                if(indexer.getDocEntities().containsKey(term) && queryTerms.get(term)!=-1){
                    fq=fq*1.5;
                }
                double ans=(fq*(k+1))/(fq+k*(1-b+b*(D/this.avgOfDocsSize)));
                rankDoc+=(ans*calculateIDF(term));
            }
            this.docsInfo.get(doc).setRank(0.999*rankDoc+0.001*this.docsInfo.get(doc).getQueryTermandDocTerm());
            this.DocsRank.add(docsInfo.get(doc));
        }
    }

    /**
     * Return the 50 documents with the highest rank
     */
    private void get50RelevantDocs(){
        PriorityQueue<DocumentQ> newList=new PriorityQueue<DocumentQ>();
        int size=DocsRank.size();
        for(int i=0;i<size && i<50 ;i++){
            newList.add(this.DocsRank.poll());
        }
        this.DocsRank=newList;
    }

    /**
     * calculate IDF parameter.
     * @param term
     * @return
     */
    private double calculateIDF(String term){
        int N=this.numOfDocs;
        int nq=this.termFreq.get(term);
        return Math.log10((N-nq+0.5)/(nq+0.5));
    }

    /**
     * read from documents dictionary all the documents details.
     * @param path
     */
    private void readDocsDictionary(String path){
        long sumDocSize=0;
        try{
            BufferedReader br=new BufferedReader(new FileReader(path+"documentDic.txt"));
            String line="";
            while((line=br.readLine())!=null){
                String[] docInfo=line.split("[|]");
                if(this.docsInfo.containsKey(docInfo[0]) && docInfo.length>4) {
                    this.docsInfo.get(docInfo[0]).setSize(Integer.parseInt(docInfo[1]));
                    this.docsInfo.get(docInfo[0]).setWordsInTitle(docInfo[4],this.isStem,this.stopWords);
                }
                sumDocSize+=Integer.parseInt(docInfo[1]);
                numOfDocs++;
            }
            br.close();
        } catch (IOException e) {
        }
        this.avgOfDocsSize=sumDocSize/numOfDocs;
    }

    /**
     * getter
     * @return
     */
    public PriorityQueue<DocumentQ> getDocsRank() {
        return DocsRank;
    }
}
