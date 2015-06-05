package Main;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import Util.FileTool;

public class Users {
	String trainFile =null;
	ArrayList<User> users;
	Map<String,Integer> userToIndexMap;
	
	Map<String, Integer> tagToIndexMap;
	ArrayList<String> indexToTagMap;
	Map<String, Integer> tagCountMap;
	
	Map<String, Integer> timeToIndexMap;
	ArrayList<String> indexToTimeMap;
	Map<String, Integer> timeCountMap;
	
	String [] years={"2008","2009"};
	String [] months={"01","02","03","04","05","06","07","08","09","10","11","12"};
	
	
	
	public Users(String trainFile){
		this.trainFile=trainFile;
		//this.users= new HashMap<String,User>();
		this.users= new ArrayList<User>();
		this.userToIndexMap= new HashMap<String,Integer>();
		
		
		//tag count
		this.tagToIndexMap = new HashMap<String,Integer>();
		this.indexToTagMap = new ArrayList<String>();
		this.tagCountMap   = new HashMap<String,Integer>();
		
		//time count
		this.timeToIndexMap = new HashMap<String,Integer>();
		this.indexToTimeMap = new ArrayList<String>();
		this.timeCountMap   = new HashMap<String,Integer>();		
		
		this.initTimeMap();
		this.readLineFile();
		//System.out.println(this.users.get("9"));
	}
	
	public void initTimeMap(){
		//do it in month level.
		
		int i=0;
		for( String year: this.years){
			for(String month: this.months){
				String timeLabel=String.format("%s-%s", year,month);
				this.timeToIndexMap.put(timeLabel , i);
				this.indexToTimeMap.add(timeLabel);
				this.timeCountMap.put(timeLabel, 0);
				i++;
			}
		}
	}
	
	
	public void readLineFile(){
		ArrayList<ArrayList<String>> userInfoLines = new ArrayList<ArrayList<String>>();
		FileTool.readLinesAsTaglist( this.trainFile, userInfoLines);
		for(ArrayList<String> userInfo: userInfoLines){
			String userId= userInfo.get(0);
			userInfo.remove(0);
			User u;
			if(this.userToIndexMap.containsKey(userId)){
				int userIndex= this.userToIndexMap.get(userId);
				u= this.users.get(userIndex);
			}else{
				u= new User(userId);
				this.userToIndexMap.put(userId, this.users.size());
				this.users.add(u);
				
			}
			
			ArrayList<String> eachAnswerInfo=userInfo;
			u.addNewAnswerPost(eachAnswerInfo,this);	
			
		}
		
		
	}
	
	

}

class User {
	String userId;
	ArrayList<AnswerPost> answerPosts;

	public User(String userId) {
		// TODO Auto-generated constructor stub
		this.userId=userId;
		this.answerPosts = new ArrayList<AnswerPost>();
	}
	
	public void addNewAnswerPost(ArrayList<String> eachAnswerInfo, Users users){
		
		String timeLabel = eachAnswerInfo.get(0).substring(0,7);
		eachAnswerInfo.remove(0);
		ArrayList<String> tagInfos = eachAnswerInfo;
		//System.out.println(timeLabel);
		int time= users.timeToIndexMap.get(timeLabel);
		int oldCount=0;
		oldCount=users.timeCountMap.get(timeLabel);
		users.timeCountMap.put(timeLabel,oldCount+1);
		int [] tags =  new int[tagInfos.size()];
		for(int i=0;i<tagInfos.size();i++	){
			String tag= tagInfos.get(i);
			if( ! users.tagToIndexMap.containsKey(tag)){
				int newIndex= users.tagToIndexMap.size();
				users.tagToIndexMap.put(tag, newIndex);
				users.tagCountMap.put(tag, 1);	
				tags[i]=newIndex;
			}else{
				tags[i]=users.tagToIndexMap.get(tag);
				oldCount= users.tagCountMap.get(tag);
				users.tagCountMap.put(tag, oldCount+1);
			}
		}
		AnswerPost ans= new AnswerPost(time,tags);
		this.answerPosts.add(ans);
		
		
	}
	
	
	
	
	
	
}

class AnswerPost{
	int [] Qtags;
	int Atime;
	public AnswerPost(int time, int[] tags) {
		// TODO Auto-generated constructor stub
		this.Atime=time;
		this.Qtags=tags;
	}
	

}

