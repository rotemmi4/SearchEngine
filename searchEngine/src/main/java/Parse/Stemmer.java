package Parse;

import org.tartarus.snowball.ext.englishStemmer;

/**
 *This class is the doing the stemming for the corpus terms if you choose that option when you run the app.
 */
public class Stemmer {
    private englishStemmer stemmer;
    public Stemmer(){
        stemmer = new englishStemmer();
    }

    public String stemString(String word){
        try {
            this.stemmer.setCurrent(word);
            if (this.stemmer.stem()) {
                String s=this.stemmer.getCurrent();
                return s;
            }
        } catch (Exception e) {
        }
        return word;
    }
}
