package Parse;

/**
 * Represent an Entity
 */
public class Entity implements Comparable {
    private int tf;
    private String term;

    /**
     * constructor
     * @param term
     * @param tf
     */
    public Entity(String term,int tf) {
        this.tf = tf;
        this.term = term;
    }

    /**
     * getter
     * @return
     */
    public int getTf() {
        return tf;
    }

    /**
     * setter
     * @param tf
     */
    public void setTf(int tf) {
        this.tf = tf;
    }

    /**
     * getter
     * @return
     */
    public String getTerm() {
        return term;
    }

    /**
     * setter
     * @param term
     */
    public void setTerm(String term) {
        this.term = term;
    }


    /**
     * Comparator which compare between two "TermsPerDoc" by the value of tf
     * @param o
     * @return
     */
    @Override
    public int compareTo(Object o) {
        if (((Entity)o).tf>this.tf)
            return -1;
        if (((Entity)o).tf<this.tf)
            return 1;
        if (((Entity)o).tf==this.tf)
            return 0;
        return 0;
    }
}