package Main;

import java.util.ArrayList;

public class QuestionPost {
	



	//ids,quid,date,acceptaid,score,taglen," ".join(taglist)," ".join(BodyP)
	
	public String qid;
	public User user;
	public String date;
	public AnswerPost acceptAnswer;// acceptaid;
	public String  acceptaid;
	public int score;
	public ArrayList<Integer> tags;
	public ArrayList<Integer> words;
	public ArrayList<AnswerPost> answers;
	
	public QuestionPost() {
		// TODO Auto-generated constructor stub
		this.answers = new ArrayList<AnswerPost>();

		
	}

}
