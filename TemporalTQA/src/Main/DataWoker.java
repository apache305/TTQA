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
import java.util.Map;

import Util.FileTool;

public class DataWoker {
	
	public String trainsource="/Users/zmeng/GoogleDriver/2015/full_data/temp_dir/alla100.2.train.txt";
	public String testsource ="/Users/zmeng/GoogleDriver/2015/full_data/temp_dir/alla100.2.test.txt";
	//public Map<String,User> useridMap;
	public Map<String,QuestionPost> quesitonMap; 
	public Map<String,AnswerPost> answerMap;
	
	public ArrayList<User> users = new ArrayList<User>();
	public Map<String,Integer> useridToIndex=new HashMap<String,Integer>();
	
	
	
	public Map<String, Integer> tagToIndexMap;
	public ArrayList<String> indexToTagMap;
	public Map<String, Integer> tagCountMap;  //all tag count
	
	public String timeLevel="Month";
	
	public Map<String, Integer> timeToIndexMap;
	public ArrayList<String> indexToTimeMap;
	public Map<String, Integer> timeCountMap;
	
	public Map<String, Integer> termToIndexMap;
	public ArrayList<String> indexToTermMap;
	public Map<String, Integer> termCountMap;
	
	


	
	
	public DataWoker(){
		//this class is used to prepare .... data.
		
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
		
		
		


	}
	
	
	
	public void ProcessOriData(){
		//it's better process line by line, don't need put them all in memeory.
		//should I use file util here? 
		//could change these later..
		
		
		this.readLinesAsTaglist(this.trainsource);
		System.out.println(this.users.size());//8109
		System.out.println(this.quesitonMap.size());//67741
		System.out.println(this.answerMap.size());//643729
		

	}
	



	
	public int newQuesitonPost(ArrayList<String> itemlist){
		//typeid,ids,quid,date,acceptaid,score,taglen," ".join(taglist)," ".join(BodyP)
		String qid=itemlist.get(1);
		String quid=itemlist.get(2);
		String date=itemlist.get(3);
		String acceptaid=itemlist.get(4);
		String score=itemlist.get(5);
		String taglen=itemlist.get(6);
		int tlen=Integer.parseInt(taglen);
		QuestionPost p= new QuestionPost();
		
		p.qid=qid;
		
		if(!this.useridToIndex.containsKey(quid)){
			User u = new User(quid);
			this.useridToIndex.put(quid, this.users.size());
			this.users.add(u);
			
		}
		User u = this.users.get(this.useridToIndex.get(quid)  );
		

		
		p.user=u;
		

		p.date=date;
		if(!acceptaid.equals("null"))	{
			p.acceptaid=acceptaid;
		}else{
			p.acceptaid=null;
		}
		p.score=Integer.parseInt(score);
		
		ArrayList<Integer> taglist= new ArrayList<Integer>();
		ArrayList<Integer> words=new ArrayList<Integer>();
		for(int i=7;i<7+tlen;i++){
			String tag= itemlist.get(i);
			if(!this.tagToIndexMap.containsKey(tag)){
				this.tagToIndexMap.put(tag,this.indexToTagMap.size())	;
				this.indexToTagMap.add(tag);
			}
			taglist.add(this.tagToIndexMap.get(tag));
		}
		p.tags=taglist;
		for(int i=7+tlen;i<itemlist.size();i++){
			String word= itemlist.get(i);
			if(!this.termToIndexMap.containsKey(word)){
				this.termToIndexMap.put(word,this.indexToTermMap.size())	;
				this.indexToTermMap.add(word);
			}
			words.add(this.termToIndexMap.get(word));
		}
		p.words=words;
		
		//put the new question into the map;
		u.questionPosts.add(p);
		this.quesitonMap.put(qid, p);
		
		return 1;

	}
	public int newAnswerPost(ArrayList<String> itemlist){
		//typeid,ids,auid,date,pid,score," ".join(BodyP)))
		
		String aid=itemlist.get(1);
		String auid=itemlist.get(2);
		String date=itemlist.get(3);
		String qid= itemlist.get(4);
		String score=itemlist.get(5);

		AnswerPost a=new AnswerPost();
		//QuestionPost p= new QuestionPost();
		this.answerMap.put(aid, a);
		
		a.aid=aid;
		if(!this.useridToIndex.containsKey(auid)){
			User u = new User(auid);
			this.useridToIndex.put(auid, this.users.size());
			this.users.add(u);
			
		}
		User u = this.users.get(this.useridToIndex.get(auid)  );

		a.user=u;
		a.date=date;
		
		a.score=Integer.parseInt(score);
		
		
		ArrayList<Integer> words=new ArrayList<Integer>();
		
		for(int i=6;i<itemlist.size();i++){
			String word= itemlist.get(i);
			if(!this.termToIndexMap.containsKey(word)){
				this.termToIndexMap.put(word,this.indexToTermMap.size())	;
				this.indexToTermMap.add(word);
			}
			words.add(this.termToIndexMap.get(word));
		}
		a.words=words;
		
		if(!this.quesitonMap.containsKey(qid)){
			return 0;
		}
		
		u.answerPosts.add(a);

		QuestionPost q= this.quesitonMap.get(qid);
		q.answers.add(a);
		a.question=q;
		if(qid.equals(q.acceptaid)){
			q.acceptAnswer=a;
		}
		return 1;

	}
	
	private void processEachLine(ArrayList<String> itemlist){
		if(itemlist.get(0).equals("1")){
			//question
			this.newQuesitonPost(itemlist);
		}else if(itemlist.get(0).equals("2")){
			//answer
			this.newAnswerPost(itemlist);
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
				this.processEachLine(itemlist);
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
		
		DataWoker debug= new DataWoker();
		debug.ProcessOriData();
		
		
	
		
		
	}

}
