package Parse;

import ReadFile.Doc;
import java.io.IOException;
import java.util.*;

/**
 * Parse class that is doing all the tokenaztion of the corpus documents
 */
public class Parse {
    private Doc document;
    private String text;
    private HashMap<String,Integer> tokens;
    private String[] words;
    private HashMap<String, String> monthsDic;
    private HashMap<String, String> amountDic;
    private HashSet<String> symbolsDic;
    private boolean isStem;
    private Stemmer stemmer;
    private Indexer indexInstance;
    private int index=0;

    HashMap<String,Integer> tempToken;

    /**
     * getter
     * @return HashMap<String, Integer>
     */
    public HashMap<String, Integer> getTokens() {
        return tokens;
    }

    /**
     * Constructor
     * @param document for the parser
     */
    public Parse(Doc document,String text, boolean isStem) throws IOException {
        this.indexInstance= Indexer.getInstance();
        this.document=document;
        this.text = text;
        this.tokens = new HashMap<String, Integer>();
        this.words=null;
        monthsDic= new HashMap<String, String>();
        amountDic= new HashMap<String, String>();
        symbolsDic=new HashSet<String>();
        initMonths();
        initAmount();
        initSymbol();
        this.isStem=isStem;
        this.stemmer=new Stemmer();
    }
    //**************************************************************************
    public Parse(String text, boolean isStem) {
        this.text = text;
        this.isStem=isStem;
        this.stemmer=new Stemmer();
        this.words=null;
        this.tempToken=new HashMap<String, Integer>();
        this.tokens = new HashMap<String, Integer>();
        monthsDic= new HashMap<String, String>();
        amountDic= new HashMap<String, String>();
        symbolsDic=new HashSet<String>();
        initMonths();
        initAmount();
        initSymbol();
    }

    /**
     * The function parse the document's text.
     */
    public void parseText(int numOfDocs, HashSet<String> stopWords) throws IOException {
        replaceSymbols();
        this.words=text.split(" ");
        int numOfWords=words.length;
        if(this.document!=null){
            document.setDocSize(numOfWords);
        }
        //remove dot from the end of word
        for (int i=0;i<numOfWords ;i++){
            if(!symbolsDic.contains(words[i]) && words[i].length()>1) {
                if (!words[i].equals("U.S.") && words[i].charAt(words[i].length() - 1) == '.') {
                    words[i] = words[i].substring(0, words[i].length() - 1);
                }
            }
        }
        for(int i=0;i<numOfWords;i++) {
            if (!symbolsDic.contains(words[i]) && words[i].length()>0) {
                String tkn = words[i];
                int wordSize = tkn.length();
                //find entities
                int countWords=0;
                if(tkn.length()>0 && !tkn.equals("") &&  !tkn.equals(" ") && (int)(tkn.charAt(0))>64 && (int)(tkn.charAt(0))<91) {
                    countWords++;
                    while (i + 1 < words.length  && countWords<2) {
                        String test=words[i+1];
                        if(!(test.equals(" ") || test.equals(""))) {
                            if (((int) (test.charAt(0))) > 64 && ((int) (test.charAt(0))) < 91) {
                                tkn += " " + words[i + 1];
                                i++;
                                countWords++;
                            } else {
                                break;
                            }
                        }
                        else{
                            i++;
                            continue;
                        }
                    }
                }
                //percent
                if (tkn.length()>1 && tkn.charAt(wordSize - 1) == '%') {
                    addToken(tkn,numOfDocs);
                    continue;
                } else if (isNumber(tkn) && i + 1 < numOfWords) {
                    if (words[i + 1].contains("percent")) {
                        tkn += "%";
                        i++;
                        addToken(tkn,numOfDocs);
                        continue;
                    }
                }
                //price
                boolean isDollars = false;
                if (tkn.charAt(0) == '$' && tkn.length()>1 && isNumber(tkn.substring(1,tkn.length()-1))) {
                    tkn = tkn.substring(1, wordSize);
                    isDollars = true;
                }
                if (tkn.length()>1&& isNumber(tkn.substring(0, tkn.length() - 1)) && tkn.charAt(tkn.length() - 1) == 'm') {
                    tkn = tkn.substring(0, tkn.length() - 1) + "M";
                }
                if (tkn.length()>2 && isNumber(tkn.substring(0, tkn.length() - 2)) && tkn.substring(tkn.length() - 2, tkn.length()).equals("bn")) {
                    tkn = tkn.substring(0, tkn.length() - 2) + "B";
                }

                //number
                if (isNumber(tkn)) {
                    //number<1000
                    if (Double.parseDouble(tkn) < 1000) {
                        if (i + 1 < numOfWords) {
                            if (isFraction(words[i + 1])) {
                                tkn = tkn + " " + words[i + 1];
                                i++;
                            } else {
                                if (amountDic.containsKey(words[i + 1])) {
                                    tkn = tkn + changeSymbol(words[i + 1]);
                                    //addToken(tkn);
                                    i++;
                                }
                                //dates
                                if (i + 1 < numOfWords && monthsDic.containsKey(words[i + 1])) {
                                    if (tkn.length() == 1) {
                                        tkn = "0" + tkn;
                                    }
                                    tkn = getMonth(words[i + 1]) + "-" + tkn;
                                    i++;
                                    addToken(tkn,numOfDocs);
                                    continue;
                                }
                            }
                        }
                    }
                    //number>1000
                    else {
                        Double num = Double.parseDouble(tkn);
                        tkn = addSymbol(num, false);
                    }
                }
                //dollar
                if (isDollars || (isNumber(tkn.substring(0,tkn.length()-1)) && ((i + 1 < numOfWords && words[i + 1].contains("Dollars")) || (i + 2 < numOfWords && words[i + 1].equals("U.S.") && words[i + 2].contains("dollars"))))) {
                    if (tkn.charAt(tkn.length() - 1) == 'M') {
                        tkn = tkn.substring(0, tkn.length() - 1) + " M";
                    } else if (tkn.charAt(tkn.length() - 1) == 'K') {
                        double p = Double.parseDouble(tkn.substring(0, tkn.length() - 1)) * 1000;
                        tkn = deleteZero(p + "");
                        tkn=addComma(tkn);
                        //tkn=p+"";
                    } else if (tkn.charAt(tkn.length() - 1) == 'B') {
                        double p = Double.parseDouble(tkn.substring(0, tkn.length() - 1)) * 1000;
                        tkn = deleteZero(p + "");
                        tkn += " M";
                    }
                    tkn = tkn + " Dollars";
                    addToken(tkn,numOfDocs);

                    if (i + 1 < numOfWords && words[i + 1].contains("Dollars")) {
                        i++;
                    } else if (i + 2 < numOfWords && words[i + 1].equals("U.S.") && words[i + 2].contains("dollars")) {
                        i += 2;
                    }
                    continue;
                }
                //dates
                if (monthsDic.containsKey(tkn) && i + 1 < numOfWords && isNumber(words[i + 1])) {
                    if (words[i + 1].length() == 1) {
                        tkn = monthsDic.get(tkn) + "-0" + words[i + 1];
                    } else if (words[i + 1].length() == 2) {
                        tkn = monthsDic.get(tkn) + "-" + words[i + 1];
                    } else if (words[i + 1].length() == 4) {
                        tkn = words[i + 1] + "-" + monthsDic.get(tkn);
                    }
                    addToken(tkn,numOfDocs);
                    i++;
                    continue;
                }
                //between
                if(tkn.equals("between") | tkn.equals("Between")) {
                    if (i + 3 < numOfWords && isNumber(words[i + 1]) && (words[i + 2].equals("and") | words[i + 2].equals("And")) && isNumber(words[i + 3])) {
                        addToken(tkn,numOfDocs);
                        addToken(words[i+1],numOfDocs);
                        addToken(words[i+2],numOfDocs);
                        addToken(words[i+3],numOfDocs);
                        addToken(tkn+" "+ words[i+1]+" "+words[i+2]+" "+words[i+3],numOfDocs);
                        i=i+3;
                        continue;
                    }
                }
                //from
                if(tkn.equals("from") | tkn.equals("From")) {
                    if (i + 3 < numOfWords && isNumber(words[i + 1]) && (words[i + 2].equals("to") | words[i + 2].equals("To")) && isNumber(words[i + 3])) {
                        addToken(tkn,numOfDocs);
                        addToken(words[i+3],numOfDocs);
                        addToken(words[i+1],numOfDocs);
                        addToken(words[i+2],numOfDocs);
                        addToken(tkn+" "+ words[i+1]+" "+words[i+2]+" "+words[i+3],numOfDocs);
                        i=i+3;
                        continue;
                    }
                }
                if(tkn.contains("-")){
                    int index=tkn.indexOf("-");
                    if(isNumber(tkn.substring(0,index))&& isNumber(tkn.substring(index+1,tkn.length()))){
                        addToken(tkn,numOfDocs);
                        continue;
                    }
                    else{
                        addToken(tkn,numOfDocs);
                        continue;
                    }
                }
                //Stemmer
                if(isStem && isWord(tkn)){
                    tkn=stemmer.stemString(tkn);
                }
                if(tkn.contains(" ")) {
                    String[] entityWords = tkn.split(" ");
                    if (entityWords.length > 1) {
                        for (int j = 0; j < entityWords.length; j++) {
                            if (!stopWords.contains(entityWords[j].toLowerCase())) {
                                addEntity(entityWords[j]);// Stemming
                            }
                        }
                        if (!stopWords.contains(tkn.toLowerCase())) {
                            addToken(tkn, numOfDocs);
                        }
                    }
                }
                if (!stopWords.contains(tkn.toLowerCase())) {
                    addToken(tkn, numOfDocs);
                }
            }
        }
        findEntities();
        if(numOfDocs!=-1 && numOfDocs!=-2) {
            updateNumberOfTems();
            updateSpecificTerm();
            indexInstance.getInstance();
            indexInstance.writeEntitiesPosting(tokens, document.getDocNo());
            mergeTokens();
            indexInstance.addTermsToTermsTree(tokens, document.getDocNo(), numOfDocs, isStem);
            indexInstance.writeToDocumentDic(document.toString());
        }
        else {
            mergeTokens();
            HashMap<String,Integer> newToken=new HashMap<String, Integer>();
            for (String token:this.tokens.keySet()) {
                newToken.put(token.toUpperCase(), this.tokens.get(token));
                newToken.put(token.toLowerCase(), this.tokens.get(token));
            }
            this.tokens=newToken;
        }
    }

    /**
     * merge between 2 Maps that help us to find entities.
     */
    private void mergeTokens() {
        if (this.tempToken != null && this.tokens != null) {
            for (String word : this.tempToken.keySet()) {
                Integer wordFreq = tempToken.get(word);
                if (tokens.containsKey(word)) {
                    Integer value = tokens.get(word);
                    tokens.replace(word, value, value + wordFreq);//increase the appearance.
                } else {
                    tokens.put(word, wordFreq);
                }
            }
            tempToken.clear();
        }
    }

    /**
     * The function find entities in the document
     */
    private void findEntities(){
        HashMap<String,Integer> temp=new HashMap<String, Integer>();
        for (String str:tokens.keySet()) { //running over the tokens in the document
            if(str.length()>0) {
                String firstLetter = str.substring(0, 1);  //takes the first letter of the token
                String word = "";
                if (((int) str.charAt(0)) > 64 && ((int) str.charAt(0)) < 91) {  //checks if the first letter is a capital letter
                    if (temp.containsKey(firstLetter.toLowerCase() + str.substring(1, str.length()))) {  //checkes if the token exist in temp
                        word = firstLetter.toLowerCase() + str.substring(1, str.length());
                        temp.put(word, tokens.get(word) + tokens.get(str));
                        continue;
                    }
                } else if (((int) str.charAt(0)) > 96 && ((int) str.charAt(0)) < 123) {  //checks if the first letter isn't a capital letter
                    if (temp.containsKey(firstLetter.toUpperCase() + str.substring(1, str.length()))) {  //checks if the toekn exist in temp
                        word = firstLetter.toUpperCase() + str.substring(1, str.length());
                        temp.put(str, tokens.get(word) + tokens.get(str));
                        temp.remove(word);
                        continue;
                    }
                }
                temp.put(str, tokens.get(str));
            }
        }
        this.tokens=temp;
    }

    /**
     * setter
     */
    private void updateNumberOfTems(){
        document.setNumOfterms(tokens.size());
    }

    /**
     * Searches for the term with the maximal frequency in the document
     */
    private void updateSpecificTerm(){
        String maxTerm="";
        int max=0;
        for (String str:tokens.keySet()) {
            if(tokens.get(str)>max){
                maxTerm=str;
                max=tokens.get(str);
            }
        }
        document.setMaxfWord(maxTerm);
        document.setMaxf(max);
    }

    /**
     * check if the String is a standard word
     * @param str
     * @return true if the String is standard word. else return false.
     */
    private boolean isWord(String str){
        for(int i=0; i<str.length();i++){
            char c=str.charAt(i);
            int val= (int)c;
            if(!((val>64 && val<91)||(val>96 && val<123) || (c==' '))){////?????????????//
                return false;
            }
        }
        return true;
    }

    /**
     * The function check if the String is a Integer.
     * @param str given String.
     * @return true if the String is a Integer, else return false.
     */
    private boolean isInteger(String str){
        for(int i=0; i<str.length();i++){
            if(Character.digit(str.charAt(i),10) < 0){
                return false;
            }
        }
        return true;
    }

    /**
     *The function check if the String is a Double.
     * @param str given String.
     * @return true if the String is a Double, else return false.
     */
    private boolean isDouble(String str){
        if(!str.contains(".") || str.length()==1)
            return false;
        int i=0;
        boolean found=false;
        for(;!found && i<str.length();i++){
            if(str.charAt(i) == '.'){
                found=true;
                i++;
                break;
            }
            else if(Character.digit(str.charAt(i),10) < 0)
                return false;
        }
        for(;i<str.length();i++){
            if(Character.digit(str.charAt(i),10) < 0)
                return false;
        }
        return true;
    }

    /**
     * The function check if the String is Number(Integer or Double).
     * @param str given String.
     * @return true if the String is a Number, else return false.
     */
    private boolean isNumber(String str){
        if(str.length()>0 ) {
            if (isInteger(str))
                return true;
            else if (isDouble(str)) {
                decreaseDecimalNumber(str);
                return true;
            }
        }
        String [] f=str.split(" ");
        if(f.length==2){
            if(isInteger(f[0])&& isFraction(f[1]))
                return true;
        }
        return false;
    }

    /**
     * method that collects all the text symbols replacements we want to do for the documents
     */
    private void replaceSymbols(){
        text=text.replaceAll("[\n\t\r]"," ");
        text=text.replaceAll("[\\?,_;|`~#&\\^\\*!\"]"," ");
        text=text.replaceAll("]","");
        text=text.replaceAll("[\\(]"," ");
        text=text.replaceAll("[\\)]"," ");
        text=text.replaceAll("[{]"," ");
        text=text.replaceAll("[}]"," ");
        text=text.replaceAll("\\[","");
        text=text.replaceAll("[\\Q..\\E]", "");
        text=text.replaceAll("[\\Q//\\E]", "");
        text=text.replaceAll("[\\Q+\\E]", "");
        text=text.replace("--"," ");
        text=text.replaceAll("'s", "");
        text=text.replaceAll("\\." + "\n", " ");
        text=text.replaceAll(" '", "");
        text=text.replaceAll("' ", "");
        text=text.replaceAll(": ", " ");
        text=text.replaceAll(":", " ");
        text=text.replaceAll("\\. \n", " ");
        text=text.replaceAll("�", "");
        text=text.replaceAll("¥", "");
        text=text.replaceAll("£", "");
        text=text.replaceAll("@", "");
        text=text.replaceAll("Æ", "");
        text=text.replaceAll("Ŭ", "");
        text=text.replaceAll("⪕", "");
        text=text.replaceAll("⪖", "");
        text=text.replaceAll("Ω", "");
        text=text.replaceAll("°", "");
        text=text.replaceAll("=", " ");
        text=text.replaceAll(">", " ");
        text=text.replaceAll("<", " ");
        text=text.replaceAll("[\\+]", " ");
        text=text.replaceAll("[ ]*-[ ]*", "-");
    }

    /**
     *The function check if the String is a Fraction.
     * @param str given String.
     * @return true if the String is a Fraction, else return false.
     */
    private boolean isFraction(String str){
        if(!str.contains("/"))
            return false;
        int i=0;
        boolean found=false;
        for(;!found && i<str.length();i++){
            if(str.charAt(i) == '/'){
                found=true;
            }
            else if(Character.digit(str.charAt(i),10) < 0)
                return false;
        }
        for(;i<str.length();i++){
            if(Character.digit(str.charAt(i),10) < 0)
                return false;
        }
        return true;
    }

    /**
     * The function receives a number and reduces its decimal digits.
     * @param str given String(NUMBER).
     * @return Decimal number represented by String.
     */
    private String decreaseDecimalNumber(String str){
        String ans="";
        boolean foundDot=false;
        int countNum=0;
        for(int i=0;i<str.length() && countNum<3 ;i++){
            if(str.charAt(i) == '.'){
                foundDot=true;
            }
            else if(foundDot==true){
                countNum++;
            }
            ans+=str.charAt(i);
        }
        ans=deleteZero(ans);
        return ans;
    }

    /**
     * The function adds a new word to the repository if it does not appear.
     * else increases its value.
     * @param word
     */
    private void addToken(String word,int numOfDocs){
        if(numOfDocs!=-1 && numOfDocs!=-2) {
            this.document.setDocSize();
        }
        if(tokens.containsKey(word)){
            Integer value= tokens.get(word);
            tokens.replace(word,value,value+1);//increase the appearance.
        }
        else{
            tokens.put(word,1);
        }
    }

    /**
     * the function add the that the entity contain in another Map
     * @param entity
     */
    private void addEntity(String entity){
        //System.out.println(entity);
        if(tempToken==null){
            tempToken=new HashMap<String, Integer>();
        }
        if(tempToken.containsKey(entity)){
            Integer value= tempToken.get(entity);
            tempToken.replace(entity,value,value+1);//increase the appearance.
        }
        else{
            tempToken.put(entity,1);
        }
    }

    /**
     * The function Clean the token
     * @param str given String.
     * @return cline String/
     */
    private String cleanToken(String str){
        String newStr=str;
        if(!str.equals("U.S.") && str.charAt(str.length()-1) == '.'){
            newStr=str.substring(0,str.length()-2);
        }
        return  newStr;
    }

    /**
     * The function add appropriate symbol for number
     * @param num
     * @param withSpace
     * @return fixed String
     */
    private String addSymbol(double num, boolean withSpace){
        String smb="";
        if(num<1000)
            return num+"";
        else {
            num=num/1000;
            if (num<1000)
                smb = "K";
            else{
                num=num/1000;
                if(num<1000)
                    smb =  "M";
                else {
                    num=num/1000;
                    smb =  "B";
                }
            }
        }
        String ans=decreaseDecimalNumber(num+"");
        if(withSpace){
            return ans+" "+smb;
        }
        else return ans+smb;
    }

    /**
     *The function change symbol for number
     * @param sym word for symbol
     * @return correct symbol.
     */
    private String changeSymbol(String sym){
        return amountDic.get(sym);
    }

    /**
     * The function get Month
     * @param mth
     * @return correct month
     */
    private String getMonth(String mth){
        return monthsDic.get(mth);
    }

    /**
     * The function delete zero from the end of the decimal number
     * @param str decimal number
     * @return fixed String
     */
    private String deleteZero (String str){
        if(!str.contains("."))
            return str;
        while (true){
            if(str.charAt(str.length()-1)=='0'){
                str=str.substring(0,str.length()-1);
            }
            else if (str.charAt(str.length()-1)== '.'){
                str=str.substring(0,str.length()-1);
                break;
            }
            else{
                break;
            }
        }
        return str;
    }

    /**
     * The function add commas to number that equals or bigger than 1000
     * @param str number
     * @return fixed str
     */
    private  String addComma (String str){
        String ans="";
        int counter=0;
        for (int i=str.length()-1;i>=0;i--){
            if(counter==3){
                counter=0;
                ans=","+ans;
            }
            ans=str.charAt(i)+ans;
            counter++;
        }
        return ans;
    }

    /**
     * Represent dictionary to amount.
     */
    private void initAmount() {
        amountDic.put("Thousand","K");
        amountDic.put("Million","M");
        amountDic.put("Trillion","B");
        amountDic.put("Billion","B");
        amountDic.put("billion","B");
        amountDic.put("million","M");
        amountDic.put("trillion","B");
        amountDic.put("thousand","K");
        amountDic.put("bn","B");
        amountDic.put("m","M");
    }

    /**
     * Represent dictionary to symbols.
     */
    private void initSymbol(){
        symbolsDic.add("--");
        symbolsDic.add(".");
        symbolsDic.add("$");
    }

    /**
     * Represent dictionary to months.
     */
    private void initMonths() {
        monthsDic.put("JAN", "01");
        monthsDic.put("Jan", "01");
        monthsDic.put("JANUARY", "01");
        monthsDic.put("January", "01");
        monthsDic.put("FEB", "02");
        monthsDic.put("Feb", "02");
        monthsDic.put("February", "02");
        monthsDic.put("FEBRUARY", "02");
        monthsDic.put("Mar", "03");
        monthsDic.put("MAR", "03");
        monthsDic.put("March", "03");
        monthsDic.put("MARCH", "03");
        monthsDic.put("Apr", "04");
        monthsDic.put("APR", "04");
        monthsDic.put("April", "04");
        monthsDic.put("APRIL", "04");
        monthsDic.put("May", "05");
        monthsDic.put("MAY", "05");
        monthsDic.put("June", "06");
        monthsDic.put("JUNE", "06");
        monthsDic.put("July", "07");
        monthsDic.put("JULY", "07");
        monthsDic.put("Aug", "08");
        monthsDic.put("AUG", "08");
        monthsDic.put("August", "08");
        monthsDic.put("AUGUST", "08");
        monthsDic.put("Sept", "9");
        monthsDic.put("SEPT", "9");
        monthsDic.put("September", "9");
        monthsDic.put("SEPTEMBER", "9");
        monthsDic.put("Oct", "10");
        monthsDic.put("OCT", "10");
        monthsDic.put("October", "10");
        monthsDic.put("OCTOBER", "10");
        monthsDic.put("Nov", "11");
        monthsDic.put("NOV", "11");
        monthsDic.put("November", "11");
        monthsDic.put("NOVEMBER", "11");
        monthsDic.put("Dec", "12");
        monthsDic.put("DEC", "12");
        monthsDic.put("December", "12");
        monthsDic.put("DECEMBER", "12");
    }


}
