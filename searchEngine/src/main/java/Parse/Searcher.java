package Parse;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

import static Parse.Semantics.getMap_concepte;

/**
 * Search Relevant document for query and rank them.
 */
public class Searcher {
    public String query;
    public Parse parse;
    private String path;
    public HashMap<String, Integer> queryTerms;
    private Indexer indexInstance=Indexer.getInstance();
    private boolean isStem;
    private boolean isSemantic;
    private HashMap<String,DocumentQ> docAndListOfTerms;//<doc, list of term and freq>
    private HashMap<String,Integer> numOfDocsToTerm;//<term,num of docs that contain this term>
    private PriorityQueue<DocumentQ> rankDocuments;
    private Ranker rankAlgorithm;
    private int queryNum;
    private boolean online;

    /**
     * constructor
     * @param query
     * @param queryNum
     * @param isStem
     * @param path
     * @param semantic
     */
    public Searcher(String query, int queryNum, boolean isStem, String path, boolean semantic, boolean online) {
        this.query = query;
        this.isStem=isStem;
        this.path=path;
        docAndListOfTerms =new HashMap<String,DocumentQ>();
        this.numOfDocsToTerm=new HashMap<String, Integer>();
        this.isSemantic=semantic;
        this.queryNum=queryNum;
        this.online=online;
    }

    /**
     * Main function. search relevant document for query.
     * @param stopWords
     * @throws IOException
     */
    public void Search(HashSet<String> stopWords) throws IOException {
        parseQuery(stopWords);
        if(isSemantic){
            findSemanticWords();
        }
        getQueryDocs();
        this.rankAlgorithm=new Ranker(this.docAndListOfTerms,this.numOfDocsToTerm,this.path,this.isStem,stopWords);
        this.rankDocuments=this.rankAlgorithm.RankQuery(this.queryTerms);
    }

    /**
     * parse the query.
     * @param stopWords
     * @throws IOException
     */
    private void parseQuery(HashSet<String> stopWords) throws IOException {
        parse=new Parse(query, isStem);
        parse.parseText(-1, stopWords);
        queryTerms=parse.getTokens();
    }

    /**
     * use semantic API to find similar words to the query's terms.
     */
    private void findSemanticWords(){
        ArrayList<String> concept=new ArrayList<>();
        ArrayList<String> semantic=new ArrayList<>();
        for (String queryWord:this.queryTerms.keySet()){
            semantic.add(queryWord);
        }
        Semantics s=new Semantics(semantic);
        if(!online){
            // s.word2Vec();
        }
        else{
            s.startConnection();
        }
        HashMap<String, ArrayList<String>> con=getMap_concepte();
        for (String sc:con.keySet()) {
            ArrayList<String> array=con.get(sc);
            for(int i=0;i<array.size();i++){
                concept.add(array.get(i));
            }
        }
        for(int i=0;i<concept.size();i++){
            this.queryTerms.put(concept.get(i),-1);
        }
    }

    /**
     * search in document dictionary the docs the contain the words in query.
     * @throws IOException
     */
    private void getQueryDocs() throws IOException {
        for (String queryTerm: this.queryTerms.keySet()) {
            if (indexInstance.getPostingDictionary().get(queryTerm) != null) {
                String[] queryTermDetails = indexInstance.getPostingDictionary().get(queryTerm).split("[|]");//line from dictionary}
                Stream<String> all_lines = Files.lines(Paths.get(path+"/"+queryTermDetails[1]));
                String specificLine = all_lines.skip(Integer.parseInt(queryTermDetails[2]) - 1).findFirst().get();//line from posting
                String[] termInfo = specificLine.split("[|]");// SPLIT- term|list of Doc:freq
                termInfo[1]=termInfo[1].substring(1,termInfo[1].length());// remove the first space
                String[] termInDoc = termInfo[1].split(" ");
                for (int i = 0; i < termInDoc.length; i++) {
                    String[] split = termInDoc[i].split(":");
                    if (!this.docAndListOfTerms.containsKey(split[0])) {
                        this.docAndListOfTerms.put(split[0], new DocumentQ(split[0]));
                    }
                    this.docAndListOfTerms.get(split[0]).addTermsAndFreq(termInfo[0], Integer.parseInt(split[1]));
                    this.numOfDocsToTerm.put(termInfo[0], termInDoc.length);
                }
            }
        }
    }

    /**
     * getter
     * @return
     */
    public PriorityQueue<DocumentQ> getRankDocuments() {
        return rankDocuments;
    }

}


