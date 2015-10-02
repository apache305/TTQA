package Main;

import java.util.ArrayList;

public class QuestionPost extends Post{
	



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
	public String acceptaid;
	public ArrayList<AnswerPost> answers;
	public AnswerPost acceptAnswer;

	
	public QuestionPost() {
		// TODO Auto-generated constructor stub
		this.typeid=1;
	}

}
