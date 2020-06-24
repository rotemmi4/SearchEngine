package Parse;

import java.io.*;
import java.util.*;

/**
 * Singleton indexer- This class add set of words to the posting files and makes the indexer dictionary and document dictionary
 */
public class Indexer {

    private static Indexer indexInstance=new Indexer() ;
    private TreeMap<String, HashMap<String,Integer>> terms;  //HashMap<term,<DocNo,frequency>>
    private String path;
    private ArrayList<String> docDic;
    private TreeMap<String,String> sortedDictionary;
    private HashMap<String,String> postingDictionary;
    public int numOfTermAllCorpus=0;
    private boolean isStem;
    private Map<String,ArrayList<Entity>> docEntities;


    /**
     * constructor
     */
    private Indexer()  {
        this.docDic=new ArrayList<String>();
        terms=new TreeMap<String, HashMap<String, Integer>>(); // alef-beth sort the treemap of tokens
        this.docEntities=new HashMap<String, ArrayList<Entity>>();
    }

    /**
     * gets the instance of indexer that is singleton
     * @return
     */
    public static Indexer getInstance(){
        return indexInstance;
    };

    /**
     * getter
     * @return
     */
    public TreeMap<String, HashMap<String, Integer>> getTerms() {
        return terms;
    }

    /**
     * adds all the term we received from the parser to TreeMap
     * @param termsFromParse
     * @param DocNo
     * @param numOfDocs
     * @param isStem
     * @throws IOException
     */
    public void addTermsToTermsTree(HashMap<String,Integer> termsFromParse, String DocNo, int numOfDocs, Boolean isStem) throws IOException {
        for (String term:termsFromParse.keySet()) {
            if(this.terms.containsKey(term)){
                this.terms.get(term).put(DocNo,termsFromParse.get(term));
            }
            else{
                this.terms.put(term,new HashMap<String, Integer>());
                this.terms.get(term).put(DocNo,termsFromParse.get(term));
            }
        }
        writeToPosting(numOfDocs, isStem);
    }

    /**
     * write to the posting files every 25000 Docs
     * @param numOfDocs
     * @param isStem
     * @throws IOException
     */
    public void writeToPosting(int numOfDocs, boolean isStem) throws IOException {
        if(!isStem) {
            if (numOfDocs%25000 == 0 ) {
                addToPosting();
                terms = new TreeMap<String, HashMap<String, Integer>>();
            }
        }
        else {
            if (numOfDocs%25000== 0 ) {
                addToPosting();
                terms = new TreeMap<String, HashMap<String, Integer>>();
            }
        }
    }

    /**
     * create the A-Z posting files
     * @param path
     * @throws IOException
     */
    public void createPosting(String path) throws IOException {
        this.path=path;
        createPostingFiles(path);
    }

    /**
     * reset all the Data Structure
     */
    public void reset(){
        this.terms=new TreeMap<>();
        this.docDic=new ArrayList<>();
        this.sortedDictionary=new TreeMap<>();
        this.postingDictionary=new HashMap<>();
        this.docEntities= new HashMap<>();
    }

    /**
     * write to posting files
     */
    public void addToPosting() throws IOException {
        String type="#Symbols&Numbers"; //
        TreeMap<String, String> setOfWords=readPostingFile(type);  //setofwords- is the terms that we load from each existing posting file
        for(String str : terms.keySet()){
            // Symbols&Numbers
            String firstLetter=str.substring(0,1)+"";
            if( !((((int)str.charAt(0))>64 && ((int)str.charAt(0))<91) || (((int)str.charAt(0))>96 && ((int)str.charAt(0))<123))) { //checks if the first letter is symbol or number
                if (setOfWords.containsKey(str)) { //if the setofwords exist then we add it and tostring its details else, we add new term
                    setOfWords.put(str, setOfWords.get(str) + getDetails(terms.get(str)));
                } else {
                    setOfWords.put(str, getDetails(terms.get(str)));
                }
            }
            //words
            else{
                if(!firstLetter.equals(type) && !type.equals(firstLetter.toUpperCase())){ //change the posting file
                    writeToPostingFile(type,setOfWords);//write to the posting
                    type=firstLetter.toUpperCase();
                    setOfWords=readPostingFile(type);
                }
                if(((int)firstLetter.charAt(0))>64 && ((int)firstLetter.charAt(0))<91){//with Capital letter
                    if (setOfWords.containsKey(str)){
                        setOfWords.put(str, setOfWords.get(str) + getDetails(terms.get(str)));
                    }
                    else if (setOfWords.containsKey(str.substring(0,1).toLowerCase()+str.substring(1,str.length()))){
                        String word=str.substring(0,1).toLowerCase()+str.substring(1,str.length());
                        setOfWords.put(word,setOfWords.get(word)+getDetails(terms.get(str)));
                    }
                    else{
                        setOfWords.put(str, getDetails(terms.get(str)));
                    }
                }
                else if (((int)firstLetter.charAt(0))>96 && ((int)firstLetter.charAt(0))<123){//without Capital letter
                    if (setOfWords.containsKey(str)){
                        setOfWords.put(str, setOfWords.get(str) + getDetails(terms.get(str)));
                    }
                    else if (setOfWords.containsKey(str.substring(0,1).toUpperCase()+str.substring(1,str.length()))){
                        String word=str.substring(0,1).toUpperCase()+str.substring(1,str.length());
                        setOfWords.put(str,setOfWords.get(word)+getDetails(terms.get(str)));
                        setOfWords.remove(word);
                    }
                    else{
                        setOfWords.put(str, getDetails(terms.get(str)));
                    }
                }
            }
        }
        writeToPostingFile(type,setOfWords);
    }

    /**
     * adding all the words to the sorted terms treemap
     * @param words
     */
    private void addWordsToTerms(HashMap<String, HashMap<String,Integer>> words){
        terms=new TreeMap<String, HashMap<String, Integer>>();
        for (String str:words.keySet()) {
            terms.put(str,words.get(str));
        }
    }

    /**
     * read all the terms from posting file
     * @param type is the name of the posting file
     * @return sorted TreeMap with the terms
     */
    private TreeMap<String,String> readPostingFile(String type){
        String line="";
        TreeMap<String, String> post=new TreeMap<String, String>();
        try(BufferedReader br=new BufferedReader(new FileReader(path+"/"+type+".txt") )) {
            while((line=br.readLine())!=null) {
                String[] splitLine=line.split("[|]");
                post.put(splitLine[0],splitLine[1]);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return post;
    }

    /**
     * write all the term sorted to A-Z posting files
     * @param type
     * @param termDetails
     * @throws IOException
     */
    private void writeToPostingFile(String type,TreeMap<String,String> termDetails) throws IOException {
        File f=new File(path+"/"+type+".txt");
        FileWriter fw = new FileWriter(f);
        BufferedWriter bw=new BufferedWriter(fw);
        StringBuilder allStr=new StringBuilder();
        for (String str : termDetails.keySet()) {
            allStr.append(str+"|"+termDetails.get(str)+"\n");
        }
        bw.append(allStr.toString());
        bw.close();
    }

    /**
     * create posting files
     * @param path to the posting files
     * @throws IOException
     */
    private void createPostingFiles(String path) throws IOException {
        FileWriter f=new FileWriter(path+"/#Symbols&Numbers.txt");
        int c=65;
        for(int i=0; i<26; i++){
            c=65;
            c=c+i;
            String type=Character.toString((char)c);
            f=new FileWriter(path+"/"+type+".txt");
        }
    }

    /**
     * The function arrange the term details
     * @param details
     * @return String - the details of the term
     */
    private String getDetails(Map<String,Integer> details){
        String ans="";
        for(String str : details.keySet()){
            ans=ans+" "+str+":"+details.get(str);
        }
        return ans;
    }

    /**
     * add and sort all the terms from the dictionary posting file to the sorted dictionary that we need to show
     * @param bf
     * @return TreeMap
     * @throws IOException
     */
    public void putAllFromDictionary(BufferedReader bf) throws IOException {
        postingDictionary=new HashMap<String, String>();
        String line = "";
        String str = "";
        String strFrequency ="";
        String strPath ="";
        String strLineNum ="";
        while ((line = bf.readLine()) != null) {
            String sb="";
            String[] splitLine = line.split("[|]");
            str=(splitLine[0]);
            strPath=(splitLine[1]);
            strLineNum=(splitLine[2]);
            strFrequency=(splitLine[3]);
            sb="|"+strPath+"|"+strLineNum+"|"+strFrequency;
            postingDictionary.put(str,sb); //***********************************
        }
    }

    /**
     * Initialize the dictionary and document posting files and writes the data to each one
     * @throws IOException
     */
    public void initDictionaries() throws IOException {
        File dir = new File(path);
        File[] files = dir.listFiles();
        String indexdic = path + "/" + "dictionary.txt";
        String filename = this.path + "/" + "documentDic.txt";
        BufferedWriter writer = new BufferedWriter(new FileWriter(indexdic, false));
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < files.length; i++) {//foreach file
            if (!files[i].getName().equals("dictionary.txt")  && !files[i].getName().equals("documentDic.txt") && !files[i].getName().equals("entities.txt")) {
                String line = "";
                try (BufferedReader bf = new BufferedReader(new FileReader(files[i]))) {
                    int lineIndex =0;
                    while ((line = bf.readLine()) != null) {
                        lineIndex++;
                        String[] splitLine=line.split("[|]");
                        String term=splitLine[0];
                        String info=splitLine[1];
                        String[] splitInfo=info.split(" ");
                        if (term.contains(" ") && !info.substring(1).contains(" "))
                            continue;
                        int countDocs=0;
                        for(int j=0; j<splitInfo.length; j++) {
                            if (!splitInfo[j].equals("")) {
                                countDocs++;
                            }
                        }
                        String word = "";
                        String[] splitLine2 = line.split("[|]");
                        word = splitLine[0];
                        if (((int) word.charAt(0)) > 64 && ((int) word.charAt(0)) < 91) {
                            word = word.toUpperCase();
                        }
                        numOfTermAllCorpus++;
                        sb.append(word + "|" + files[i].getName()+ "|" + lineIndex + "|" + countDocs + "\n");
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        try {
            BufferedWriter writer2 = new BufferedWriter(new FileWriter(filename, true));
            for (int i = 0; i < this.docDic.size(); i++) {
                writer2.append(this.docDic.get(i) + "\n");
            }
            writer2.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        writer.write(sb.toString());
        writer.close();
    }

    /**
     * add data to the document dictionary
     * @param str
     */
    public void writeToDocumentDic(String str) {
        this.docDic.add(str);
    }

    /**
     * getter
     * @return int
     */
    public int getNumOfTermAllCorpus() {
        return numOfTermAllCorpus;
    }

    /**
     * finds the term show number in a document
     * @param str
     * @return int
     */
    private int findFrequency(String str){
        int sum=0;
        String []docs=str.split(" ");
        for(int i=1;i<docs.length;i++){
            String []dtl=docs[i].split("[:]");
            sum+=Integer.parseInt(dtl[1]);
        }
        return sum;
    }

    /**
     * load the dictionary
     * @param path
     * @param stem
     * @throws IOException
     */
    public void loadFrequenctDic(String path, boolean stem) throws IOException {
        if (stem) {
            BufferedReader bf = new BufferedReader(new FileReader(path+"/"+"dictionary.txt"));
            putAllFromDictionary(bf);
            Indexer.getInstance().sortedDictionary=new TreeMap<String, String>(postingDictionary);
            bf.close();
        }
        else {
            BufferedReader bf = new BufferedReader(new FileReader(path+"/"+"dictionary.txt"));
            putAllFromDictionary(bf);
            Indexer.getInstance().sortedDictionary=new TreeMap<String, String>(postingDictionary);
            bf.close();
        }
        findTop5Entities(path);
    }

    /**
     * getter
     * @return sorted TreeMap
     */
    public TreeMap<String, String> getSortedDictionary() {
        return sortedDictionary;
    }

    /**
     * getter
     * @return
     */
    public HashMap<String, String> getPostingDictionary() {
        return postingDictionary;
    }

    /**
     * write all the document's entities into file.
     * @param tokens
     * @param DocNo
     * @throws IOException
     */
    public void writeEntitiesPosting(HashMap<String, Integer> tokens, String DocNo) throws IOException {
        String post = path + "/" + "entities.txt";
        BufferedWriter writerEnt = new BufferedWriter(new FileWriter(post, true));
        StringBuilder sb = new StringBuilder();
        sb.append(DocNo + "|" );
        for (String entity: tokens.keySet()) {
            if((int)(entity.charAt(0))>64 && (int)(entity.charAt(0))<91) {
                sb.append(entity.toUpperCase()+":"+tokens.get(entity)+"&");
            }
        }
        sb.append("\n");
        writerEnt.append(sb.toString());
        writerEnt.close();
    }

    /**
     * the function find to all the document the 5 entities with biggest frequency.
     * @param path
     * @throws IOException
     */
    public void findTop5Entities(String path) throws IOException {
        PriorityQueue<Entity> entitiesQue = new PriorityQueue<Entity>();
        BufferedReader br = new BufferedReader(new FileReader(path + "/entities.txt"));
        String line = "";
        while ((line = br.readLine()) != null) {
            String[] splitEntity = line.split("[|]");
            if(  splitEntity.length>1 && splitEntity[1]!=null){
                String docno = splitEntity[0];
                DocumentQ doc = new DocumentQ(docno);
                String entities = splitEntity[1];
                String[] splitEntityNames = entities.split("&");
                for (int i = 0; i < splitEntityNames.length; i++) {
                    String[] finalSlpit = splitEntityNames[i].split(":");
                    String entityName = finalSlpit[0];
                    String entityFreq = finalSlpit[1];
                    if (postingDictionary.get(entityName) != null) {
                        if (entitiesQue.size() < 6) {
                            Entity ent = new Entity(entityName, Integer.parseInt(entityFreq));
                            entitiesQue.add(ent);
                        } else if (entitiesQue.peek().getTf() < Integer.parseInt(entityFreq)) {
                            entitiesQue.poll();
                            Entity ent = new Entity(entityName, Integer.parseInt(entityFreq));
                            entitiesQue.add(ent);
                        }
                    } else {
                        continue;
                    }
                }
                ArrayList<Entity> tmp = new ArrayList<Entity>();
                ArrayList<Entity> ent = new ArrayList<Entity>();
                for (int i = 0; i < 5; i++) {
                    tmp.add(entitiesQue.poll());
                }
                for (int i = 4; i >= 0; i--) {
                    ent.add(tmp.get(i));
                }
                this.docEntities.put(docno, ent);
            }
        }
        br.close();
    }

    /**
     * getter
     * @return
     */
    public Map<String, ArrayList<Entity>> getDocEntities() {
        return docEntities;
    }
}
