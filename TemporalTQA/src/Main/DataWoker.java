package Main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import Util.FileTool;

public class DataWoker {
	
	public String datasource="/Users/zmeng/GoogleDriver/2015/full_data/temp_dir/all_in_one.txt";
	public Map<String,User> useridMap;
	public Map<String,QuestionPost> quesitonMap;
	public Map<String,AnswerPost> answerMap;
	
	
	public DataWoker(){
		//this class is used to prepare .... data.
		
	}
	
	
	
	public void ProcessOriData(){
		//it's better process line by line, don't need put them all in memeory.
		//should I use file util here? 
		//could change these later..
		
		
		this.readLinesAsTaglist(this.datasource);
		
	}
	public void newQuesitonPost(ArrayList<String> itemlist){
		
		
	}
	public void newAnswerPost(ArrayList<String> itemlist){
		
	}
	public void newUser(ArrayList<String> itemlist){
		
	}
	private void processEachLine(ArrayList<String> itemlist){
		if(itemlist.get(0)=="1"){
			//question
			this.newQuesitonPost(itemlist);
		}else if(itemlist.get(0)=="2"){
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
	
	
	public void readLineFile(){
		ArrayList<ArrayList<String>> userInfoLines = new ArrayList<ArrayList<String>>();
		FileTool.readLinesAsTaglist( this.trainFile, userInfoLines);
		for(ArrayList<String> userInfo: userInfoLines)
			
	}
	
	public static void main(String [] args){
		//get question posts
		//get answer posts
		
		
		
		
		
		

		
		
	}

}
