package Main;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import Util.FileTool;

public class Users {
	public String trainFile =null;
	public ArrayList<User> users;
	public Map<String,Integer> userToIndexMap;
	
	public Map<String, Integer> tagToIndexMap;
	public ArrayList<String> indexToTagMap;
	public Map<String, Integer> tagCountMap;
	
	public Map<String, Integer> timeToIndexMap;
	public ArrayList<String> indexToTimeMap;
	public Map<String, Integer> timeCountMap;
	
	public int voteStep;
	public double [] voteMap;
	
	public String [] years={"2008","2009"};
	public String [] months={"01","02","03","04","05","06","07","08","09","10","11","12"};
	
	
	
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
		
		//vote count
		this.voteStep=3;// low medium high
		this.voteMap = new double [this.voteStep];
		
		
		this.initTimeMap();
		this.initVoteMap();
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
	
	public void initVoteMap(){
		//first to find the vote freq map;
		ArrayList<ArrayList<String>> userInfoLines = new ArrayList<ArrayList<String>>();
		FileTool.readLinesAsTaglist( this.trainFile, userInfoLines);
		//Map<Array>
		// I jsut want to simplfied here. not to complated.
		int max_vote=0;
		for(ArrayList<String> userInfo: userInfoLines){
			//String userId= userInfo.get(0);
			int score =Integer.parseInt(userInfo.get(3)) ;
			max_vote= Math.max(score, max_vote);
			//System.out.println(score);
		}
		//System.out.println(max_vote);
		double one_step=  Math.log(max_vote)/(double)this.voteStep;
		for (int i=0;i<this.voteStep;i++){
			this.voteMap[i]=(i+1)*one_step;
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





