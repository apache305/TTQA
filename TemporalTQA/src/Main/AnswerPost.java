package Main;

import java.util.ArrayList;

public class AnswerPost  extends Post{
	
	public int typeid;
	public String id;
	public int uid;
	public int dateid;
	public String date;
	public int score;
	public int vote_level;
	public User user;
	public ArrayList<Integer> words;
	public ArrayList<Integer> tags;
	public QuestionPost question;
	public AnswerPost(){
		this.typeid=2;
		
	}

	
}