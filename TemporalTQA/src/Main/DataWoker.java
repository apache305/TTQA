package Main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import Util.FileTool;

public class DataWoker {
	
	
	public String datasource;
	//public Map<String,User> useridMap;
	public Map<String,QuestionPost> quesitonMap; 
	public Map<String,AnswerPost> answerMap;
	
	public ArrayList<User> users = new ArrayList<User>();
	public Map<String,Integer> useridToIndex=new HashMap<String,Integer>();
	
	
	
	public Map<String, Integer> tagToIndexMap;
	public ArrayList<String> indexToTagMap;
	public Map<String, Integer> tagCountMap;  //all tag count
	
	public int timeLevel=7;//"Month";
	
	public Map<String, Integer> timeToIndexMap;
	public ArrayList<String> indexToTimeMap;
	public Map<String, Integer> timeCountMap;
	
	public Map<String, Integer> termToIndexMap;
	public ArrayList<String> indexToTermMap;
	public Map<String, Integer> termCountMap;
	
	public Map<String,Integer> voteToIndexMap;
	public ArrayList<String> indexToVoteMap;
	public Map<String, Integer> voteCountMap;
	
	public int maxvote=0;//max vote score.
	public int voteStep=3;//low, medium, high.
	public double [] voteMap;//only for our model.
	
	public int singleOccDocument[];
	public int coOccDocument[][];
	
	public DataWoker(){
		
	}
	
	
	


	
	
	public DataWoker(String datasource){
		//this class is used to prepare .... data.
		this.datasource=datasource;
		
		this.users= new ArrayList<User>();
		this.useridToIndex = new HashMap<String,Integer>();

		this.quesitonMap=new HashMap<String,QuestionPost>();
		this.answerMap = new HashMap<String,AnswerPost>();
		
		
		//tag count
		this.tagToIndexMap = new HashMap<String,Integer>();
		this.indexToTagMap = new ArrayList<String>();
		this.tagCountMap   = new HashMap<String,Integer>();
		
		//term count
		this.termToIndexMap = new HashMap<String,Integer>();
		this.indexToTermMap = new ArrayList<String>();
		this.termCountMap   = new HashMap<String,Integer>();
		
		//time count
		this.timeToIndexMap = new HashMap<String,Integer>();
		this.indexToTimeMap = new ArrayList<String>();
		this.timeCountMap   = new HashMap<String,Integer>();	
		
		
		//time count
		this.voteToIndexMap = new HashMap<String,Integer>();
		this.indexToVoteMap = new ArrayList<String>();
		this.voteCountMap   = new HashMap<String,Integer>();	

		
		this.voteMap = new double [this.voteStep];
		
		
		


	}
	public void initVoteMap(){
		//first to find the vote freq map;
		
		//System.out.println(max_vote);
		double one_step=  Math.log(this.maxvote)/(double)this.voteStep;
		for (int i=0;i<this.voteStep;i++){
			this.voteMap[i]=(i+1)*one_step;
		}
		

		
	}
	
	public int getVoteLevel(int score){
		double cur_vote_log=0.0 ;
		if (score>=0){
			cur_vote_log=Math.log(score);
		}else{
			return 0;
		}
		int vote_level=0;
		for(;vote_level< this.voteStep ;vote_level++){
			if( cur_vote_log <= this.voteMap[vote_level]){
				break;
			}
		}
		if(vote_level==this.voteStep){
			return this.voteStep-1;
		}else{
			return vote_level;
		}
	
	}
	
	public void updateAllVoteLevel(){
		for(QuestionPost q : this.quesitonMap.values()){
			q.vote_level= this.getVoteLevel(q.score);
		}
		for(AnswerPost a : this.answerMap.values()){
			a.vote_level=this.getVoteLevel(a.score);
		}
	}
	
	
	public void ProcessOriData(){
		//it's better process line by line, don't need put them all in memeory.
		//should I use file util here? 
		//could change these later..
		
		
		this.readLinesAsTaglist(this.datasource);
		this.initVoteMap();
		
		

		

	}
	
	public void printStat(){
		System.out.println(this.users.size());//8109
		System.out.println(this.quesitonMap.size());//67741
		System.out.println(this.answerMap.size());//643729
	}
	
	
	public int newPost(ArrayList<String> itemlist){
		String typeid=itemlist.get(0);
		String id=itemlist.get(1);
		String uid=itemlist.get(2);
		String date=itemlist.get(3);
		String score=itemlist.get(4);
		
		
		
		if(!this.useridToIndex.containsKey(uid)){
			User u = new User(uid);
			this.useridToIndex.put(uid, this.users.size());
			this.users.add(u);
		}
		User u = this.users.get(this.useridToIndex.get(uid)  );
		Post p = new Post();
		p.user=u;
		p.id=id;
		
		

		String month=date.substring(0, this.timeLevel);
		
		if(!this.timeToIndexMap.containsKey(month)){
			this.timeToIndexMap.put(month,this.indexToTimeMap.size());
			this.indexToTimeMap.add(month);
			this.timeCountMap.put(month, 0);
		}
		int oldTimeCount = this.timeCountMap.get(month);
		this.timeCountMap.put(month, oldTimeCount+1);
		p.dateid=this.timeToIndexMap.get(month);
		p.date=date;

		p.score=Integer.parseInt(score);
		
		String taglen=itemlist.get(5);
		int tlen=Integer.parseInt(taglen);
		
		ArrayList<Integer> taglist= new ArrayList<Integer>();
		ArrayList<Integer> words=new ArrayList<Integer>();
		for(int i=6;i<6+tlen;i++){
			String tag= itemlist.get(i);
			if(!this.tagToIndexMap.containsKey(tag)){
				this.tagToIndexMap.put(tag,this.indexToTagMap.size())	;
				this.indexToTagMap.add(tag);
				this.tagCountMap.put(tag, 0);
			}
			int oldCount= this.tagCountMap.get(tag);
			this.tagCountMap.put(tag, oldCount+1);
			taglist.add(this.tagToIndexMap.get(tag));
		}
		p.tags=taglist;
		for(int i=6+tlen;i<itemlist.size();i++){
			String word= itemlist.get(i);
			if(!this.termToIndexMap.containsKey(word)){
				this.termToIndexMap.put(word,this.indexToTermMap.size())	;
				this.indexToTermMap.add(word);
				this.termCountMap.put(word, 0);
			}
			int oldCount= this.termCountMap.get(word);
			this.termCountMap.put(word, oldCount+1);
			words.add(this.termToIndexMap.get(word));
		}
		p.words=words;
		
		u.allPosts.add(p);

		
		
		return 1;

		
		
		
	}
	



	
	
	public void computeCoOccur(){
		//this.cooc= new int[this.termCountMap.size()][this.termCountMap.size()];
		
		this.singleOccDocument= new int [this.termCountMap.size()];
		this.coOccDocument= new int [this.termCountMap.size()][this.termCountMap.size()];
		
		for (User u : this.users){
			for(Post eachPost : u.allPosts){
				ArrayList<Integer> words=eachPost.words;
				Set<Integer> filter=new HashSet<Integer>();
				for(int i=0;i<words.size();i++){
					filter.add(words.get(i));//each words id.
				}
				ArrayList<Integer> filterWords= new ArrayList<Integer>();
				for(int wid: filter){
					filterWords.add(wid);
				}

				for(int i=0;i<filterWords.size();i++){
					singleOccDocument[filterWords.get(i) ]++;

					for(int j=i+1;j<filterWords.size();j++	){
						this.coOccDocument[filterWords.get(i)][filterWords.get(j)]++;
						//this.cooc[ words.get(i)][words.get(j)]++;
					}
				}
			}
		}
	}
	
	private void processEachLine(ArrayList<String> itemlist){
		
		if(itemlist.get(0).equals("1")){
			//question
			//this.newQuesitonPost(itemlist);
		}else if(itemlist.get(0).equals("2")){
			//answer
			//this.newAnswerPost(itemlist);
		}

	}
	
	public void readLinesAsTaglist(String file){
		BufferedReader reader=null;
		String [] parts=null;
		ArrayList<String> itemlist=null;
		try{
			reader=new BufferedReader(new FileReader(new File(file)));
			String eachLine=null;
			while( (eachLine=reader.readLine())!=null){
				parts=eachLine.split(" ");
				itemlist=new ArrayList<String>(Arrays.asList(parts));
				//process each itemlist
				this.newPost(itemlist);
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if(reader!=null){
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
	}
	

	
	public static void main(String [] args){
		//get question posts
		//get answer posts
		String trainsource="/Users/zmeng/GoogleDriver/2015/full_data/temp_dir/alla100.2.train.txt";
		String testsource ="/Users/zmeng/GoogleDriver/2015/full_data/temp_dir/alla100.2.test.txt";
		DataWoker debug1= new DataWoker(trainsource);
		DataWoker debug2=new DataWoker(testsource);
		debug1.ProcessOriData();
		debug1.printStat();
		//for
		debug2.ProcessOriData();
		debug2.printStat();

		
	}

}
