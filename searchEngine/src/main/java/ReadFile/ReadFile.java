package ReadFile;

import Parse.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.*;
import java.util.HashMap;
import java.util.HashSet;

/**
 * This class read all the documents from given path
 */
public class ReadFile {

    private String path;
    private HashMap<String,Doc> corpus;   //<DocNo,Doc>
    private HashSet<String> stopWords;
    public Parse parser;
    private boolean isStem;
    private Indexer indexInstance= Indexer.getInstance();
    public int numOfDocs=0;

    /**
     * Constructor
     *
     * @param p given path
     * @throws IOException
     */
    public ReadFile(String p, boolean isStem) throws IOException {
        this.path = p;
        this.corpus = new HashMap<String, Doc>();
        this.stopWords = new HashSet<String>();
        this.isStem=isStem;
        readStopWordsFile(new File(path+"/stop_words.txt"));
        readFiles();
    }

    /**
     * The function read files from given path
     *
     * @throws IOException
     */
    public void readFiles() throws IOException {
        File dir = new File(path);
        readFiles(dir);
    }

    /**
     * The function read files from given path
     *
     * @param dir given directory
     * @throws IOException
     */
    private void readFiles(File dir) throws IOException {
        File[] files = dir.listFiles();
        int i = 0;
        for (i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) {
                readFiles(files[i]);
            }
            else if (!files[i].getName().equals("stop_words.txt")){
                readDocs(files[i]);
            }
        }
        if(indexInstance.getTerms().size()>0 && i>2){
            indexInstance.addToPosting();
            indexInstance.getTerms().clear();
        }
    }

    public void setNumOfDocs(int numOfDocs) {
        this.numOfDocs = numOfDocs;
    }

    /**
     * The function read the stopwords file/
     * @param file stop words file
     * @throws FileNotFoundException
     */
    private void readStopWordsFile(File file) throws FileNotFoundException {
        String line = "";
        try (BufferedReader bf = new BufferedReader(new FileReader(file.getPath()))) {
            while ((line = bf.readLine()) != null) {
                line=line.replace("\n","");
                if(!line.equals("may") && !line.equals("between"))
                    stopWords.add(line);
            }
        } catch (IOException e) {
        }
    }

    /**
     *The function reads the file and split to documents according to the tags
     * @param file to read. This file contain documents.
     * @throws IOException
     */
    private void readDocs(File file) throws IOException {
        Document doc;
        doc= Jsoup.parse(file, "UTF-8");
        Elements tags=doc.select("DOC");
        for(Element element : tags) {
            String text = element.select("TEXT").text();
            String docNo = element.select("DOCNO").text();
            String title = element.select("H3").select("TI").text();
            String date = element.select("DATE").text();
            numOfDocs++;
            parser = new Parse(new Doc(path, docNo, title, date), text, isStem);
            parser.parseText(numOfDocs, this.stopWords);
        }
    }

    /**
     * getter
     * @return int - docs number
     */
    public int getNumOfDocs() {
        return numOfDocs;
    }

    /**
     * Get function.
     *
     * @return the path of the directory.
     */
    public String getPath() {
        return path;
    }

    /**
     * Get function.
     *
     * @return the corpus.
     */
    public HashMap<String,Doc> getCorpus() {
        return corpus;
    }

    public HashSet<String> getStopWords() {
        return stopWords;
    }
}
