#include<iostream>
#include<fstream>
#include<vector>
#include<string>
#include<algorithm>
#include<queue>
#include<unordered_map>
#include<chrono>
#include<set>
using namespace std;

class TrieNode{
public:
    unordered_map<char,TrieNode*>children;
    bool isEndOfWord;
    int frequency;
    TrieNode(){
        isEndOfWord=false;
        frequency=0;
    }
};

class Trie{
private:
    TrieNode*root;
    int totalWords;

    void deleteTrieNode(TrieNode*node){
        if(!node)return;
        for(auto&pair:node->children){
            deleteTrieNode(pair.second);
        }
        delete node;
    }

    int minEditDistance(const string&word1,const string&word2,int maxDist){
        int m=word1.length();
        int n=word2.length();
        if(abs(m-n)>maxDist)return maxDist+1;
        vector<vector<int>>dp(2,vector<int>(n+1));
        for(int j=0;j<=n;j++){
            dp[0][j]=j;
        }
        for(int i=1;i<=m;i++){
            int curr=i&1;
            int prev=1-curr;
            dp[curr][0]=i;
            int minVal=i;
            for(int j=1;j<=n;j++){
                if(word1[i-1]==word2[j-1]){
                    dp[curr][j]=dp[prev][j-1];
                }else{
                    dp[curr][j]=1+min({dp[prev][j],dp[curr][j-1],dp[prev][j-1]});
                }
                minVal=min(minVal,dp[curr][j]);
            }
            if(minVal>maxDist)return maxDist+1;
        }
        return dp[m&1][n];
    }

    void dfsSearch(TrieNode*node,string currentWord,const string&target,int maxDist,vector<pair<string,int>>&results,set<string>&seen){
        if(node->isEndOfWord&&currentWord.length()>0){
            if(seen.find(currentWord)==seen.end()){
                int dist=minEditDistance(target,currentWord,maxDist);
                if(dist<=maxDist){
                    results.push_back({currentWord,node->frequency});
                    seen.insert(currentWord);
                }
            }
        }
        for(auto&pair:node->children){
            dfsSearch(pair.second,currentWord+pair.first,target,maxDist,results,seen);
        }
    }

public:
    Trie(){
        root=new TrieNode();
        totalWords=0;
    }

    ~Trie(){
        deleteTrieNode(root);
    }

    void insert(const string&word){
        TrieNode*current=root;
        for(char ch:word){
            if(current->children.find(ch)==current->children.end()){
                current->children[ch]=new TrieNode();
            }
            current=current->children[ch];
        }
        if(!current->isEndOfWord){
            totalWords++;
        }
        current->isEndOfWord=true;
        current->frequency++;
    }

    bool search(const string&word){
        TrieNode*current=root;
        for(char ch:word){
            if(current->children.find(ch)==current->children.end()){
                return false;
            }
            current=current->children[ch];
        }
        return current->isEndOfWord;
    }

    vector<string>autoCorrect(const string&word,int maxDist=2){
        if(search(word)){
            return {word};
        }
        vector<pair<string,int>>results;
        set<string>seen;
        dfsSearch(root,"",word,maxDist,results,seen);
        sort(results.begin(),results.end(),[](const pair<string,int>&a,const pair<string,int>&b){
            return a.second>b.second;
        });
        vector<string>suggestions;
        int limit=min(5,(int)results.size());
        for(int i=0;i<limit;i++){
            suggestions.push_back(results[i].first);
        }
        return suggestions;
    }

    int getWordCount(){
        return totalWords;
    }
};

class AutoCorrector{
private:
    Trie trie;
    string dictionaryFile;

    bool loadDictionary(){
        ifstream file(dictionaryFile);
        if(!file.is_open()){
            cerr<<"Error: Could not open dictionary file: "<<dictionaryFile<<endl;
            return false;
        }
        string word;
        int count=0;
        while(file>>word){
            string cleanWord="";
            for(char ch:word){
                if(isalpha(ch)){
                    cleanWord+=tolower(ch);
                }
            }
            if(cleanWord.length()>0){
                trie.insert(cleanWord);
                count++;
            }
        }
        file.close();
        cout<<"Loaded "<<count<<" words from dictionary."<<endl;
        return true;
    }

public:
    AutoCorrector(const string&dictFile):dictionaryFile(dictFile){
        if(!loadDictionary()){
            cerr<<"Warning: Dictionary not loaded properly."<<endl;
        }
    }

    vector<string>correctWord(const string&word){
        string cleanWord="";
        for(char ch:word){
            if(isalpha(ch)){
                cleanWord+=tolower(ch);
            }
        }
        if(cleanWord.empty()){
            return {};
        }
        return trie.autoCorrect(cleanWord);
    }

    void runBenchmark(int numQueries){
        vector<string>testWords={"hello","wrold","speling","corect","programing","algorith","structur","efficent","optimze","implementaion"};
        auto start=chrono::high_resolution_clock::now();
        for(int i=0;i<numQueries;i++){
            string word=testWords[i%testWords.size()];
            vector<string>suggestions=correctWord(word);
        }
        auto end=chrono::high_resolution_clock::now();
        auto duration=chrono::duration_cast<chrono::milliseconds>(end-start);
        double avgTime=(double)duration.count()/numQueries;
        cout<<"Benchmark Results:"<<endl;
        cout<<"Total Queries: "<<numQueries<<endl;
        cout<<"Total Time: "<<duration.count()<<" ms"<<endl;
        cout<<"Average Time per Query: "<<avgTime<<" ms"<<endl;
        cout<<"Queries per Second: "<<(int)(numQueries/(duration.count()/1000.0))<<endl;
    }

    void interactiveMode(){
        string input;
        cout<<"\nAutocorrect System (type 'exit' to quit, 'benchmark' to run tests)"<<endl;
        while(true){
            cout<<"\nEnter word: ";
            getline(cin,input);
            if(input=="exit"){
                break;
            }
            if(input=="benchmark"){
                runBenchmark(1000);
                continue;
            }
            auto start=chrono::high_resolution_clock::now();
            vector<string>suggestions=correctWord(input);
            auto end=chrono::high_resolution_clock::now();
            auto duration=chrono::duration_cast<chrono::microseconds>(end-start);
            if(suggestions.empty()){
                cout<<"No suggestions found."<<endl;
            }else if(suggestions[0]==input){
                cout<<"✓ Correct spelling!"<<endl;
            }else{
                cout<<"Did you mean: ";
                for(size_t i=0;i<suggestions.size();i++){
                    cout<<suggestions[i];
                    if(i<suggestions.size()-1)cout<<", ";
                }
                cout<<endl;
            }
            cout<<"Response time: "<<duration.count()/1000.0<<" ms"<<endl;
        }
    }
};

int main(int argc,char*argv[]){
    string dictionaryFile="dictionary.txt";
    if(argc>1){
        dictionaryFile=argv[1];
    }
    cout<<"Trie-Based Autocorrector with Edit Distance"<<endl;
    cout<<"==========================================="<<endl;
    AutoCorrector corrector(dictionaryFile);
    corrector.interactiveMode();
    return 0;
}
