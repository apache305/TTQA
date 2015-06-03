package Main;

import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;

import Util.FileTool;

public class Users {
	String trainFile =null;
	ArrayList< ArrayList<String> > users;
	
	public Users(String trainFile){
		this.trainFile=trainFile;
		this.readLineFile();
	}
	
	
	public void readLineFile(){
		ArrayList<ArrayList<String>> users = new ArrayList<ArrayList<String>>();
		FileTool.readLinesAsTaglist( this.trainFile, users);
		//System.out.println(users.get(0));
	}
	
	

}

class User {
	
	
	
	
	
}

