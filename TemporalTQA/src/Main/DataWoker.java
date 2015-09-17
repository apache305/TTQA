package Main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import Util.FileTool;

public class DataWoker {
	
	public String datasource="/Users/zmeng/GoogleDriver/2015/full_data/temp_dir/all_in_one.txt";
	public Map<String,User> useridMap;
	public Map<String,QuestionPost> quesitonMap;
	public Map<String,AnswerPost> answerMap;
	
	
	public DataWoker(){
		//this class is used to prepare .... data.
		
		this.useridMap= new HashMap<String,User>();
		this.quesitonMap=new HashMap<String,QuestionPost>();
		this.answerMap = new HashMap<String,AnswerPost>();
		
	}
	
	
	
	public void ProcessOriData(){
		//it's better process line by line, don't need put them all in memeory.
		//should I use file util here? 
		//could change these later..
		
		
		this.readLinesAsTaglist(this.datasource);
		
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
		if(!this.useridMap.containsKey(quid)){
			//new user
			User u = new User(quid);
			this.useridMap.put(quid, u);
		}
		User u= this.useridMap.get(quid);
		
		p.user=u;
		

		p.date=date;
		if(!acceptaid.equals("null"))	{
			p.acceptaid=acceptaid;
		}else{
			p.acceptaid=null;
		}
		p.score=Integer.parseInt(score);
		
		ArrayList<String> taglist= new ArrayList<String>();
		ArrayList<String> words=new ArrayList<String>();
		for(int i=7;i<7+tlen;i++){
			taglist.add(itemlist.get(i));
		}
		p.tags=taglist;
		for(int i=7+tlen;i<itemlist.size();i++){
			words.add(itemlist.get(i));
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
		
		a.aid=aid;
		if(!this.useridMap.containsKey(auid)){
			//new user
			User u = new User(auid);
			this.useridMap.put(auid, u);
		}
		User u= this.useridMap.get(auid);
		a.user=u;
		a.date=date;
		
		a.score=Integer.parseInt(score);
		
		
		ArrayList<String> words=new ArrayList<String>();
		
		for(int i=6;i<itemlist.size();i++){
			words.add(itemlist.get(i));
		}
		a.words=words;
		
		if(!this.quesitonMap.containsKey(qid)){
			return 0;
		}

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
