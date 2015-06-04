package Main;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import Util.FileTool;

public class Users {
	String trainFile =null;
	ArrayList< User > users;
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
		this.users= new ArrayList<User>();
		this.tagToIndexMap = new HashMap<String,Integer>();
		this.indexToTagMap = new ArrayList<String>();
		this.tagCountMap   = new HashMap<String,Integer>();
		
		this.timeToIndexMap = new HashMap<String,Integer>();
		this.indexToTimeMap = new ArrayList<String>();
		this.timeCountMap   = new HashMap<String,Integer>();		
		
		this.initTimeMap();
		this.readLineFile();
	}
	
	public void initTimeMap(){
		//do it in month level.
		
		int i=0;
		for( String year: this.years){
			for(String month: this.months){
				String timeLabel=String.format("%s-%s", year,month);
				this.timeToIndexMap.put(timeLabel , i);
				this.indexToTimeMap.add(timeLabel);
				i++;
			}
		}
	}
	
	
	public void readLineFile(){
		ArrayList<ArrayList<String>> userInfoLines = new ArrayList<ArrayList<String>>();
		FileTool.readLinesAsTaglist( this.trainFile, userInfoLines);
		for(ArrayList<String> userInfo: userInfoLines){
			String userId= userInfo.get(0);
			String timeLabel= userInfo.get(1).substring(0,6);
			userInfo.remove(0);
			userInfo.remove(1);
			User u= new User();
			
			
			
			
			
		}
		
		
		
		//System.out.println(users.get(0));
	}
	
	

}

class User {
	
	
	
	
	
	
}

