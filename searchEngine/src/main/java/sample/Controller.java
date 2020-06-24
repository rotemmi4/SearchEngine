package sample;

import Parse.*;
import ReadFile.Doc;
import ReadFile.ReadFile;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.stage.Modality;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.net.URL;
import java.nio.file.FileSystemNotFoundException;
import java.sql.SQLOutput;
import java.util.*;

/**
 * This class is responsible for linking the UI to the program logic
 */
public class Controller implements Initializable{

    public Button run;
    public Button reset;
    public Button loadDictionary;
    public Button showDictionary;
    public Button loadCorpus;
    public Button loadPosting;
    public TextField corpusField;
    public TextField postingField;
    public CheckBox stemmingCheckBox;
    public boolean stem;
    public Doc doc;
    public Indexer indexInstance=Indexer.getInstance();
    public String postingPath = "";
    private String corpusPath = "";
    public String newPostingPath = "";
    private File file;
    public HashSet<String> StopWords;
    public ReadQuery queryFile;
    private String queryFilePath;
    public ListView<Node> listOfDocNos;
    public boolean semantics = false;
    public CheckBox semanticsCheckBox;
    public Button queryFileLoader;
    public TextField queryUserText;
    public Button runQuery;
    public Button runQueryFile;
    public Button Trec_Eval;
    public Label queryNum;
    private  Searcher searcher;
    public HashMap<Integer, String> docs;
    private TreeMap<Integer, PriorityQueue<DocumentQ>> queryInfo;
    private int queryNumber;
    public Button Next;
    public Button Prev;
    StringBuilder sb;
    public boolean online = false;
    public CheckBox onlineCheckBox;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.docs= new HashMap<Integer, String>();
        this.queryInfo=new  TreeMap<Integer, PriorityQueue<DocumentQ>>();
        stemmingCheckBox.setSelected(false);
        StopWords=new HashSet<String>();
        Next.setVisible(false);
        Prev.setVisible(false);
        queryNum.setVisible(false);
    }

    /**
     * Get the folder path
     */
    private String folderPath() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File selectedDirectory = directoryChooser.showDialog(null);
        if (selectedDirectory == null) {
            return null;
        }
        else {
            return selectedDirectory.getAbsolutePath();
        }
    }

    /**
     * path for the corpus folder
     * @param event
     */
    public void openCorpus(ActionEvent event) {
        String path = folderPath();
        if (path != null) {
            corpusPath = path;
            corpusField.setText(path);
        }
    }

    /**
     * folder to save the posting files
     * @return
     */
    private String savePostingFiles() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("text file", "*.txt"));
        File selectedFile = fileChooser.showSaveDialog(null);
        if (selectedFile == null) {
            return null;
        } else {
            return selectedFile.getAbsolutePath();
        }
    }

    /**
     * Choose in the checkbox if to use stem or not
     */
    public void stemming(ActionEvent event) {
        if (stemmingCheckBox.isSelected())
            stem = true;
        else
            stem = false;
    }

    /**
     * path where to save the posting files
     */
    public void postingPath(ActionEvent event) {
        String path = folderPath();
        if (path != null) {
            postingPath = path;
            postingField.setText(path);
        }
    }

    /**
     * open file by source
     */
    private String chooseSource() {
        String path;
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("text file", "*.txt"));
        File f = fileChooser.showOpenDialog(null);
        if (f != null) {
            path = f.getPath();
            return path;
        } else {
            return null;
        }
    }

    /**
     * reset all
     * @param event
     * @throws IOException
     */
    public void reset(ActionEvent event) throws IOException {
        if (!postingPath.equals("")) {
            try {
                FileUtils.deleteDirectory(new File(postingPath + "/Stem"));
                FileUtils.deleteDirectory(new File(postingPath + "/WithOutStem"));
            } catch (IOException e) {
            }
            indexInstance.reset();
            newPostingPath = "";
            semanticsCheckBox.setSelected(false);
            stemmingCheckBox.setSelected(false);
            onlineCheckBox.setSelected(false);
            docs = new HashMap<>();
            queryInfo = new TreeMap<>();
            queryNum.setText("");
            listOfDocNos.getItems().clear();
            queryUserText.setText("");
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Info Dialog");
            alert.setHeaderText("APP Message");
            alert.setContentText("Reset is Done!");
            alert.show();
        }
    }

    /**
     * running the indexer
     * @param event
     * @throws IOException
     */
    public void runIndexer(ActionEvent event) throws IOException {
        try{
            float first = System.nanoTime();
            newPostingPath = postingPath;
            if (corpusField.getText().trim().isEmpty() || postingField.getText().trim().isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("APP Dialog");
                alert.setHeaderText("System Message");
                alert.setContentText("NOTICE: You Must Fill All The Fields");
                alert.show();
            } else {
                if (!stem) {
                    file = new File(postingPath + File.separator + "WithOutStem");
                    newPostingPath = postingPath + "/" + "WithOutStem";

                } else {
                    file = new File(postingPath + File.separator + "Stem");
                    newPostingPath = postingPath + "/" + "Stem";
                }
                file.mkdir();
                indexInstance.createPosting(newPostingPath);
                ReadFile fileReader = new ReadFile(corpusPath, stem);
                StopWords = fileReader.getStopWords();
                indexInstance.initDictionaries();
                if (stem) {
                    String Stem = (postingPath + "/" + "Stem" + "/");
                    indexInstance.loadFrequenctDic(Stem, stem);
                } else {
                    String withOutStem = (postingPath + "/" + "WithOutStem" + "/");
                    indexInstance.loadFrequenctDic(withOutStem, stem);
                }
                float last = System.nanoTime();
                doc = new Doc();
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setHeaderText("Indexer Info");
                alert.setContentText("Documents Number: " + fileReader.getNumOfDocs() + "\n" + "Terms Number: " + indexInstance.getNumOfTermAllCorpus() + "\n" + "Time: " + ((last - first) * Math.pow(10, -9)) + " sec");
                alert.show();
            }
        }
        catch(Exception e){
            Alert  alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Info Dialog");
            alert.setHeaderText("APP Message");
            alert.setContentText("Corpus/StopWords Not Found");
            alert.show();
        }
    }

    /**
     * show the dictionary
     * @param event
     */
    public void showDictionary(ActionEvent event) {
        try {
            if (indexInstance.getSortedDictionary()==null || indexInstance.getSortedDictionary().size()==0) {
                loadDictionary();
            }
            Stage stage = new Stage();
            stage.setTitle("Dictionary");
            FXMLLoader fxmlLoader = new FXMLLoader();
            Parent root = fxmlLoader.load(getClass().getClassLoader().getResource("ShowDictionary.fxml"));
            Scene scene = new Scene(root, 600, 500);
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("APP Dialog");
            alert.setHeaderText("System Message");
            alert.setContentText("NOTICE: File NOT Found! \n You Need Run The APP First Or Load A Dictionary");
            alert.show();
        }
    }

    /**
     * load the dictionary
     */
    public void loadDictionary() {
        if (postingPath != null && !postingPath.equals("")) {
            try {
                if(stem){
                    String Stem=(postingPath+ "/" + "Stem" + "/");
                    indexInstance.loadFrequenctDic(Stem, stem);
                }
                else{
                    String withOutStem=(postingPath+ "/" + "WithOutStem" + "/");
                    indexInstance.loadFrequenctDic(withOutStem, stem);
                }
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Info Dialog");
                alert.setHeaderText("APP Message");
                alert.setContentText("Done Loading!");
                alert.showAndWait();
            } catch (Exception e) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("APP Dialog");
                alert.setHeaderText("System Message");
                alert.setContentText("NOTICE: File NOT Found! \n You Need Run The APP First Or Load A Dictionary");
                alert.show();
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("APP Dialog");
            alert.setHeaderText("System Message");
            alert.setContentText("NOTICE: File NOT Found! \n You Need Run The APP First Or Load A Dictionary");
            alert.show();
        }
    }

    /**
     * action to choose dictionary with queries file.
     * @param event
     */
    public void browseQueryFile(ActionEvent event) {
        queryFilePath = chooseSource();
    }

    /**
     * user option to use semantic
     * @param event
     */
    public void semantics(ActionEvent event) {
        if (semanticsCheckBox.isSelected())
            semantics= true;
        else
            semantics = false;
    }

    /**
     * user option to use online semantic
     * @param event
     */
    public void online(ActionEvent event) {
        if (onlineCheckBox.isSelected()) {
            online = true;
        }
        else
            online = false;

    }

    /**
     * allow to run a single query from user
     * @param event
     * @throws IOException
     */
    public void runQueryFromUser(ActionEvent event) throws IOException {
        try {
            Next.setVisible(false);
            queryNum.setVisible(false);
            Prev.setVisible(false);
            queryNum.setText("");
            listOfDocNos.getItems().clear();
            String query = "";
            query = queryUserText.getText();
            boolean stem = stemmingCheckBox.isSelected();
            boolean semantic = semanticsCheckBox.isSelected();
            String Stem = "";
            String withOutStem = "";
            listOfDocNos.getItems().clear();
            if (postingPath != null && !postingPath.equals("")) {
                if (stem) {
                    Stem = (postingPath + "/" + "Stem" + "/");
                    searcher = new Searcher(query, 1, stem, Stem, semantic, online);
                } else {
                    withOutStem = (postingPath + "/" + "WithOutStem" + "/");
                    searcher = new Searcher(query, 1, stem, withOutStem, semantic, online);
                }
            }
            searcher.Search(StopWords);
            showAllDocs(searcher.getRankDocuments());
            writeUserQueryTrecEvalResults(searcher.getRankDocuments());
            Trec_Eval.setDisable(false);
            if(query.equals("")){
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Info Dialog");
                alert.setHeaderText("APP Message");
                alert.setContentText("You Need To Enter A Query");
                alert.show();
            }
            else{
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Info Dialog");
                alert.setHeaderText("APP Message");
                alert.setContentText("Done!");
                alert.show();
            }
        }
        catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("APP Dialog");
            alert.setHeaderText("System Message");
            alert.setContentText("Error: Dictionary Is Not Loaded Or You Have to Enter a Query");
            alert.show();
        }
        catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("APP Dialog");
            alert.setHeaderText("System Message");
            alert.setContentText("Error: Dictionary Is Not Loaded Or You Have to Enter a Query");
            alert.show();
        }
    }

    /**
     * allow to see the top 5 dominant entities for doc
     * @param event
     */
    private void OnClickEntities(ActionEvent event) {
        Button b = ((Button) event.getSource());
        int i = Integer.parseInt(b.getId());
        String docName = docs.get(i);
        ArrayList<Entity> entitiesArray=indexInstance.getDocEntities().get(docName);
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Entities");
        alert.setHeaderText("TOP 5 Dominant Entities:");
        String entites = "";
        for (Entity ent : entitiesArray) {
            entites = entites + "\n" + ent.getTerm() + "  " + ent.getTf();
        }
        alert.setContentText(entites);
        alert.showAndWait();
    }

    /**
     * show only the 50 highest relevant document that return from ranker
     */
    private void showAllDocs(PriorityQueue<DocumentQ> docno) {
        listOfDocNos.getItems().clear();
        int index = 0;
        for (DocumentQ doc : docno) {
            HBox hBox = new HBox();
            hBox.resize(526, 267 / 4);
            Button button = new Button();
            button.setText("Show Entities");
            button.resize(61, 31);
            button.setId("" + index);
            button.setOnAction(this::OnClickEntities);
            Label docName = new Label();
            docName.setText("   " + doc.getDocNo());
            docs.put(index, doc.getDocNo());
            hBox.getChildren().add(button);
            hBox.getChildren().add(docName);
            listOfDocNos.getItems().add((hBox));
            index++;
        }
    }

    /**
     * allow to run a file with  queries from user
     * @param
     * @throws FileNotFoundException
     */
    public void  runBrowseQuery(ActionEvent event) throws IOException {
        if(queryFilePath==null){
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("APP Dialog");
            alert.setHeaderText("System Message");
            alert.setContentText("No Query File Was Found!");
            alert.show();
        }
        try {
            listOfDocNos.getItems().clear();
            String Stem = "";
            String withOutStem = "";
            if (postingPath != null && !postingPath.equals("") && queryFilePath != null) {
                File file = new File(queryFilePath);
                if (stem) {
                    Stem = (postingPath + "/" + "Stem" + "/");
                    queryFile = new ReadQuery(file, Stem, stem, semantics, online);

                } else {
                    withOutStem = (postingPath + "/" + "WithOutStem" + "/");
                    queryFile = new ReadQuery(file, withOutStem, stem, semantics, online);
                }
                queryFile.readQuery(StopWords);
                queryInfo = queryFile.getQueryInfo();
                searcher = queryFile.getSearch();
                Next.setVisible(true);
                queryNum.setVisible(true);
                Prev.setVisible(true);
                queryNumber = queryInfo.firstKey();
                showAllDocs(queryInfo.get(queryNumber));
                queryNum.setText("" + queryNumber);
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Info Dialog");
                alert.setHeaderText("APP Message");
                alert.setContentText("Done!");
                alert.show();
                writeTrecEvalResults();
            }
        }
        catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("APP Dialog");
            alert.setHeaderText("System Message");
            alert.setContentText("Dictionary Is Not Loaded OR The File Is Not A Query File");
            alert.show();
            Next.setVisible(false);
            Prev.setVisible(false);
            queryNum.setText("");
        }
        catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("APP Dialog");
            alert.setHeaderText("System Message");
            alert.setContentText("Dictionary Is Not Loaded OR The File Is Not A Query File");
            alert.show();
            Next.setVisible(false);
            Prev.setVisible(false);
            queryNum.setText("");
        }
    }

    /**
     * allow to display foreach query from the file that we loaded the 50 relevant documents.
     * @param event
     */
    public void next(ActionEvent event) {
        if(queryInfo.higherKey(queryNumber) !=null){
            queryNumber = queryInfo.higherKey(queryNumber);
        }
        showAllDocs(queryInfo.get(queryNumber));
        queryNum.setText(""+queryNumber);
    }
    /**
     * allow to display foreach query from the file that we loaded the 50 relevant documents.
     * @param event
     */
    public void prev(ActionEvent event) {
        if(queryInfo.lowerKey(queryNumber) !=null){
            queryNumber = queryInfo.lowerKey(queryNumber);
        }
        showAllDocs(queryInfo.get(queryNumber));
        queryNum.setText(""+queryNumber);
    }

    /**
     * save the results of the query file we read into file with treceval format.
     */
    private void writeTrecEvalResults() {
        sb = new StringBuilder();
        for (Map.Entry<Integer, PriorityQueue<DocumentQ>> map : queryInfo.entrySet()) {
            {
                for (DocumentQ doc : map.getValue()) {
                    sb.append(map.getKey() + " 0 " + doc.getDocNo() + " 1 42.38 mt" + System.lineSeparator());
                }
            }
        }
    }

    /**
     * save the results of the user into file with treceval format.
     */
    private void writeUserQueryTrecEvalResults(PriorityQueue<DocumentQ> docs) {
        sb = new StringBuilder();
        {
            for (DocumentQ doc : docs) {
                sb.append("000" + " 0 " + doc.getDocNo() + " 1 42.38 mt" + System.lineSeparator());
            }
        }
    }
    /**
     * the button that write into file with treceval format.
     */
    public void writeTrecEvalResultsButton(ActionEvent event){
        String path = savePostingFiles();
        try {
            if(path!=null && path.length()>0) {
                File f = new File(path);
                PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(f, false)));
                writer.write(sb.toString());
                writer.close();
            }
            else {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("APP Dialog");
                alert.setHeaderText("System Message");
                alert.setContentText("You Need To Choose A File");
                alert.show();
            }
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("APP Dialog");
            alert.setHeaderText("System Message");
            alert.setContentText("You Need To Choose A File");
            alert.show();
        }
    }
}
