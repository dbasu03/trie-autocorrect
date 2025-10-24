class TrieNode{
  private TrieNode[]children;
  private boolean isEndOfWord;
  private int frequency;
  public TrieNode(){
    children=new TrieNode[26];
    isEndOfWord=false;
    frequency=0;
  }
  public TrieNode[]getChildren(){
    return children;
  }
  public boolean isEndOfWord(){
    return isEndOfWord;
  }
  public void setEndOfWord(boolean endOfWord){
    isEndOfWord=endOfWord;
  }
  public int getFrequency(){
    return frequency;
  }
  public void setFrequency(int frequency){
    this.frequency=frequency;
  }
  public void incrementFrequency(){
    this.frequency++;
  }
}
class Trie{
  private TrieNode root;
  private int wordCount;
  public Trie(){
    root=new TrieNode();
    wordCount=0;
  }
  public void insert(String word){
    if(word==null||word.isEmpty()){
      return;
    }
    word=word.toLowerCase();
    TrieNode current=root;
    for(int i=0;i<word.length();i++){
      char c=word.charAt(i);
      if(c<'a'||c>'z'){
        continue;
      }
      int index=c-'a';
      if(current.getChildren()[index]==null){
        current.getChildren()[index]=new TrieNode();
      }
      current=current.getChildren()[index];
    }
    if(!current.isEndOfWord()){
      current.setEndOfWord(true);
      wordCount++;
    }
    current.incrementFrequency();
  }
  public boolean search(String word){
    if(word==null||word.isEmpty()){
      return false;
    }
    word=word.toLowerCase();
    TrieNode node=searchNode(word);
    return node!=null&&node.isEndOfWord();
  }
  public boolean startsWith(String prefix){
    if(prefix==null||prefix.isEmpty()){
      return false;
    }
    prefix=prefix.toLowerCase();
    return searchNode(prefix)!=null;
  }
  private TrieNode searchNode(String str){
    TrieNode current=root;
    for(int i=0;i<str.length();i++){
      char c=str.charAt(i);
      if(c<'a'||c>'z'){
        continue;
      }
      int index=c-'a';
      if(current.getChildren()[index]==null){
        return null;
      }
      current=current.getChildren()[index];
    }
    return current;
  }
  public int getWordCount(){
    return wordCount;
  }
}
class AutoCorrect{
  private Trie dictionary;
  private static final int MAX_EDIT_DISTANCE=2;
  public AutoCorrect(){
    dictionary=new Trie();
  }
  public void loadDictionary(String[]words){
    for(String word:words){
      dictionary.insert(word);
    }
  }
  public void addWord(String word){
    dictionary.insert(word);
  }
  public String[]getSuggestions(String word,int maxSuggestions){
    if(word==null||word.isEmpty()){
      return new String[0];
    }
    word=word.toLowerCase();
    if(dictionary.search(word)){
      return new String[]{word};
    }
    java.util.List<SuggestionResult>suggestions=new java.util.ArrayList<>();
    findSuggestions(word,suggestions,maxSuggestions);
    suggestions.sort((a,b)->{
      if(a.distance!=b.distance){
        return Integer.compare(a.distance,b.distance);
      }
      return Integer.compare(b.frequency,a.frequency);
    });
    java.util.List<String>results=new java.util.ArrayList<>();
    for(int i=0;i<Math.min(maxSuggestions,suggestions.size());i++){
      results.add(suggestions.get(i).word);
    }
    return results.toArray(new String[0]);
  }
  private void findSuggestions(String word,java.util.List<SuggestionResult>suggestions,int maxSuggestions){
    collectAllWords(root,"",word,suggestions,maxSuggestions);
  }
  private void collectAllWords(TrieNode node,String currentWord,String target,java.util.List<SuggestionResult>suggestions,int maxSuggestions){
    if(node==null){
      return;
    }
    if(node.isEndOfWord()){
      int distance=editDistance(currentWord,target);
      if(distance<=MAX_EDIT_DISTANCE){
        suggestions.add(new SuggestionResult(currentWord,distance,node.getFrequency()));
      }
    }
    if(suggestions.size()>=maxSuggestions*10){
      return;
    }
    for(int i=0;i<26;i++){
      if(node.getChildren()[i]!=null){
        char c=(char)('a'+i);
        collectAllWords(node.getChildren()[i],currentWord+c,target,suggestions,maxSuggestions);
      }
    }
  }
  private int editDistance(String s1,String s2){
    int m=s1.length();
    int n=s2.length();
    if(m==0)return n;
    if(n==0)return m;
    int[]prev=new int[n+1];
    int[]curr=new int[n+1];
    for(int j=0;j<=n;j++){
      prev[j]=j;
    }
    for(int i=1;i<=m;i++){
      curr[0]=i;
      for(int j=1;j<=n;j++){
        if(s1.charAt(i-1)==s2.charAt(j-1)){
          curr[j]=prev[j-1];
        }else{
          int insert=curr[j-1]+1;
          int delete=prev[j]+1;
          int replace=prev[j-1]+1;
          curr[j]=Math.min(insert,Math.min(delete,replace));
        }
      }
      int[]temp=prev;
      prev=curr;
      curr=temp;
    }
    return prev[n];
  }
  private static class SuggestionResult{
    String word;
    int distance;
    int frequency;
    SuggestionResult(String word,int distance,int frequency){
      this.word=word;
      this.distance=distance;
      this.frequency=frequency;
    }
  }
}
class SpellCheckerSystem{
  private AutoCorrect autoCorrect;
  private long totalQueries;
  private long correctResults;
  public SpellCheckerSystem(){
    autoCorrect=new AutoCorrect();
    totalQueries=0;
    correctResults=0;
  }
  public void initialize(String[]dictionary){
    long startTime=System.nanoTime();
    autoCorrect.loadDictionary(dictionary);
    long endTime=System.nanoTime();
    System.out.println("Dictionary loaded: "+dictionary.length+" words in "+((endTime-startTime)/1000000)+" ms");
  }
  public String[]processQuery(String word){
    totalQueries++;
    long startTime=System.nanoTime();
    String[]suggestions=autoCorrect.getSuggestions(word,5);
    long endTime=System.nanoTime();
    long responseTime=(endTime-startTime)/1000000;
    if(suggestions.length>0){
      correctResults++;
    }
    return suggestions;
  }
  public void addWord(String word){
    autoCorrect.addWord(word);
  }
  public double getAccuracy(){
    if(totalQueries==0){
      return 0.0;
    }
    return(correctResults*100.0)/totalQueries;
  }
  public long getTotalQueries(){
    return totalQueries;
  }
  public void printStats(){
    System.out.println("Total Queries: "+totalQueries);
    System.out.println("Successful Results: "+correctResults);
    System.out.printf("Accuracy: %.2f%%\n",getAccuracy());
  }
}
public class TrieAutoCorrectSystem{
  public static void main(String[]args){
    SpellCheckerSystem system=new SpellCheckerSystem();
    String[]dictionary=generateLargeDictionary(500000);
    system.initialize(dictionary);
    String[]testQueries={"progrm","algoritm","strutcure","performnce","optimiztion","datbase","applicaton","implmentation","efficency","scalabilty"};
    System.out.println("\nProcessing test queries:");
    for(String query:testQueries){
      long startTime=System.nanoTime();
      String[]suggestions=system.processQuery(query);
      long endTime=System.nanoTime();
      System.out.printf("Query: '%s' -> Suggestions: %s (Response time: %.3f ms)\n",query,java.util.Arrays.toString(suggestions),(endTime-startTime)/1000000.0);
    }
    System.out.println("\nRunning performance test with 1000 queries...");
    long perfStartTime=System.currentTimeMillis();
    for(int i=0;i<1000;i++){
      String query=testQueries[i%testQueries.length];
      system.processQuery(query);
    }
    long perfEndTime=System.currentTimeMillis();
    double queriesPerSecond=1000.0/((perfEndTime-perfStartTime)/1000.0);
    System.out.printf("Performance: %.2f queries/second\n",queriesPerSecond);
    System.out.println("\nSystem Statistics:");
    system.printStats();
  }
  private static String[]generateLargeDictionary(int size){
    String[]words=new String[size];
    String[]baseWords={"program","algorithm","structure","performance","optimization","database","application","implementation","efficiency","scalability","development","architecture","framework","integration","deployment","configuration","authentication","authorization","encryption","validation","testing","debugging","refactoring","maintenance","documentation","repository","version","control","pipeline","container","orchestration","microservice","middleware","interface","protocol","network","security","infrastructure","monitoring","logging","analytics","processing","computing","storage","memory","cache","queue","stream","batch","real","time","synchronous","asynchronous","concurrent","parallel","distributed","scalable","reliable","available","consistent","durable","transaction","isolation","atomicity","consistency","durability","serializable","snapshot","commit","rollback","recovery","backup","restore","migration","replication","sharding","partitioning","indexing","query","execution","planning","optimization","normalization","denormalization","schema","model","entity","relationship","attribute","constraint","foreign","primary","unique","composite","clustered","nonclustered"};
    for(int i=0;i<size;i++){
      if(i<baseWords.length){
        words[i]=baseWords[i];
      }else{
        words[i]=baseWords[i%baseWords.length]+i;
      }
    }
    return words;
  }
}
