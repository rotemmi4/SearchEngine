package Parse;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Read the queries from File.
 */
public class ReadQuery {

    private File file;
    private Searcher search;
    private boolean isStem;
    private String path;
    private boolean isSemantic;
    private TreeMap<Integer, PriorityQueue<DocumentQ>> queryInfo;
    private boolean online;


    /**
     * constructor.
     * @param file
     * @param path
     * @param isStem
     * @param isSemantic
     */
    public ReadQuery(File file, String path, boolean isStem, boolean isSemantic,boolean online) {
        this.file = file;
        this.path=path;
        this.isStem=isStem;
        this.isSemantic=isSemantic;
        this.queryInfo= new TreeMap<Integer, PriorityQueue<DocumentQ>>();
        this.online=online;
    }

    /**
     * Parsing query file with jsoup
     * @throws IOException
     */
    public void readQuery(HashSet<String> stopWords) throws IOException {
        Document q;
        q = Jsoup.parse(file, "UTF-8");
        Elements queryTags = q.select("top");
        for (Element element : queryTags) {
            String title = element.select("title").text();
            String[] num = element.select("num").text().split("Number: ");
            String queryNum = num[1].split(" ")[0];
            search=new Searcher(title, Integer.parseInt(queryNum), isStem, path, isSemantic,online);
            search.Search(stopWords);
            queryInfo.put(Integer.parseInt(queryNum), search.getRankDocuments());
        }
    }

    /**
     * getter
     * @return
     */
    public TreeMap<Integer, PriorityQueue<DocumentQ>> getQueryInfo() {
        return queryInfo;
    }

    /**
     * getter
     * @return
     */
    public Searcher getSearch() {
        return search;
    }
}
