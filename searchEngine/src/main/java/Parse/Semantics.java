package Parse;

import com.medallia.word2vec.Searcher;
import com.medallia.word2vec.Word2VecModel;
import javafx.scene.control.Alert;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Semantic class to find Synonyms for query.
 * The api is datamuse.com/api to look for simillar words
 */
public class Semantics {

    private static HashMap<String, ArrayList<String>> concept;
    public Object[] json;
    private ArrayList<String> query;

    public Semantics(ArrayList<String> query) {
        concept = new HashMap<>();
        this.query = query;
    }

    /**
     * connection for the api
     */
    public void startConnection() {
        for (String term : query) {
            String URL = "https://api.datamuse.com/words?ml=" + term;
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(URL).build();
            Response res = null;
            try {
                res = client.newCall(request).execute();
            } catch (IOException e) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Semantic");
                alert.setHeaderText("No Connection To The Net!");
                alert.show();
            }
            JSONParser parser = new JSONParser();
            Object o = null;
            try {
                try {
                    o = parser.parse(res.body().string());
                    res.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (o != null) {
                    String word = "";
                    int i = 3;
                    if(term.charAt(0)>=65 && term.charAt(0)<=90)
                        i = 4;
                    json = ((JSONArray) o).toArray();
                    int index = 0;
                    concept.put(term, new ArrayList<>());
                    for (Object obj : json) {
                        word = (String) ((JSONObject) obj).get("word");
                        concept.get(term).add(index, word);
                        if (index == i)
                            break;
                        index++;
                    }
                }
            } catch (org.json.simple.parser.ParseException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * getter
     * @return
     */
    public static HashMap<String, ArrayList<String>> getMap_concepte() {
        return concept;
    }

    /**
     * offline word2Vec semantics
     */
    public void word2Vec(){
        try {
            String path="src/main/resources/word2vec.c.output.model.txt";
            File file=new File(path);
            Word2VecModel model = Word2VecModel.fromTextFile(file);
            com.medallia.word2vec.Searcher semanticSearcher = model.forSearch();
            for (String word:this.query) {
                concept.put(word,new ArrayList<String>());
                List<Searcher.Match> matches =null;
                try {
                    matches = semanticSearcher.getMatches(word, 3);
                }catch (com.medallia.word2vec.Searcher.UnknownWordException e){
                }
                if(matches!=null) {
                    int index = 0;
                    for (com.medallia.word2vec.Searcher.Match match : matches) {
                        concept.get(word).add(index, match.match());
                        index++;
                    }
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
