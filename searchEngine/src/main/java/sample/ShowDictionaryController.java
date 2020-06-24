package sample;

import Parse.Indexer;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.TreeMap;

/**
 * This class is responsible for linking the UI to the program logic and ccontrols the show dictionary app window
 */
public class ShowDictionaryController implements Initializable {
    public TextArea textArea;
    public ListView<String> listView;
    public Indexer indexInstance=Indexer.getInstance();
    public TreeMap<String,String> currDic;

    public void initialize(URL location, ResourceBundle resources) {
        currDic = indexInstance.getSortedDictionary();
        for (String str: currDic.keySet()){
            String[] splitLine=currDic.get(str).toString().split("[|]");

            listView.getItems().add(str + "      "+splitLine[3]  + "\n");
        }
    }
}
